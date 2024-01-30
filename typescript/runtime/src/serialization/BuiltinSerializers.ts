/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
import {PrimitiveKind} from "./core/SerialKind";
import {TsSerializer} from "./TsSerializer";
import {Encoder} from "./core/encoding/Encoder";
import {CompositeDecoder, Decoder, DECODER_DECODE_DONE} from "./core/encoding/Decoding";
import {PrimitiveSerialDescriptor} from "./builtins/PrimitiveSerialDescriptor";
import {SerialDescriptor} from "./core/SerialDescriptor";
import {ArrayDesc, RecordDesc} from "./internal/CollectionDescriptors";
import {CollectionSerializer, MapLikeSerializer} from "./internal/CollectionSerializers";
import dayjs, {Dayjs} from "dayjs";
import {Duration} from "dayjs/plugin/duration";
import {NullableSerializerDescriptor} from "./builtins/NullableSerializerDescriptor";
import any = jasmine.any;
import {TupleDescriptor} from "./descriptors/TupleDescriptor";
import {recordToArray} from "ts-minimum";
import {ByteArrayBuilder, PrimitiveArraySerializer, UByteArrayBuilder} from "./internal/PrimitiveArraySerializer";
import {CompositeEncoder} from "./core/encoding/Encoding";

export const StringSerializer: TsSerializer<string> = {
    descriptor: new PrimitiveSerialDescriptor("javascript.string", PrimitiveKind.STRING),
    serialize(encoder: Encoder, value: string) {
        encoder.encodeString(value)
    },
    deserialize(decoder: Decoder): string {
        return decoder.decodeString()
    }
}

export const BooleanSerializer: TsSerializer<boolean> = {
    descriptor: new PrimitiveSerialDescriptor("javascript.boolean", PrimitiveKind.BOOLEAN),
    serialize(encoder: Encoder, value: boolean): void {
        encoder.encodeBoolean(value);
    },
    deserialize(decoder: Decoder): boolean {
        return decoder.decodeBoolean();
    }
};
export const NumberSerializer: TsSerializer<number> = {
    descriptor: new PrimitiveSerialDescriptor("javascript.number", PrimitiveKind.NUMBER),
    serialize(encoder: Encoder, value: number): void {
        encoder.encodeNumber(value);
    },
    deserialize(decoder: Decoder): number {
        return decoder.decodeNumber();
    }
};

class Int8ArraySerializerClass extends PrimitiveArraySerializer<number, Int8Array, ByteArrayBuilder> {
    constructor() {
        super(NumberSerializer);
    }
    collectionSize(collection: Int8Array): number {
        return collection.length;
    }

    toBuilder(collection: Int8Array): ByteArrayBuilder {
        return new ByteArrayBuilder(collection);
    }

    empty(): Int8Array {
        return new Int8Array(0);
    }

    readElement(decoder: CompositeDecoder, index: number, builder: ByteArrayBuilder, checkIndex: boolean) {
        builder.append(decoder.decodeNumberElement(this.descriptor, index));
    }

    writeContent(encoder: CompositeEncoder, content: Int8Array, size: number) {
        for (let i = 0; i < size; i++) {
            encoder.encodeNumberElement(this.descriptor, i, content[i]);
        }
    }
}
export const Int8ArraySerializer: TsSerializer<Int8Array> = new Int8ArraySerializerClass()


// The encodeNumberElement ordeal will probably need to become encodeByteElement to better support binary formats.
class UInt8ArraySerializerClass extends PrimitiveArraySerializer<number,Uint8Array, UByteArrayBuilder> {
    constructor() {
        super(NumberSerializer);
    }
    collectionSize(collection: Uint8Array): number {
        return collection.length;
    }

    toBuilder(collection: Uint8Array): UByteArrayBuilder {
        return new UByteArrayBuilder(collection);
    }

    empty(): Uint8Array {
        return new Uint8Array(0);
    }

    readElement(decoder: CompositeDecoder, index: number, builder: UByteArrayBuilder, checkIndex: boolean) {
        builder.append(decoder.decodeNumberElement(this.descriptor, index));
    }

    writeContent(encoder: CompositeEncoder, content: Uint8Array, size: number) {
        for (let i = 0; i < size; i++) {
            encoder.encodeNumberElement(this.descriptor, i, content[i]);
        }
    }
}
export const UInt8ArraySerializer: TsSerializer<Uint8Array> = new UInt8ArraySerializerClass()



export class ArraySerializer<E> extends CollectionSerializer<E, Array<E>, Array<E>> {
    constructor(element: TsSerializer<E>) {
        super(element);
    }

    get descriptor(): SerialDescriptor {
        return new ArrayDesc(this.elementSerializer.descriptor);
    }

    builder(): Array<E> {
        return [];
    }

    builderSize(builder: Array<E>): number {
        return builder.length;
    }

    toResult(builder: Array<E>): Array<E> {
        return builder;
    }

    toBuilder(collection: Array<E>): Array<E> {
        // In TypeScript, arrays are always mutable, so we can return the original array
        return collection;
    }

    checkCapacity(builder: Array<E>, size: number): void {
        // JavaScript arrays do not have a method like 'ensureCapacity',
        // as they are dynamically sized. This method could be left empty
        // or implement logic if needed for specific cases.
    }

    insert(builder: Array<E>, index: number, element: E): void {
        builder.splice(index, 0, element);
    }
}


export class RecordSerializer<K extends string | number, V> extends MapLikeSerializer<K, V, Record<K,V>> {
    descriptor: SerialDescriptor;

    constructor(kSerializer: TsSerializer<K>, vSerializer: TsSerializer<V>) {
        super(kSerializer, vSerializer);
        this.descriptor = new RecordDesc(kSerializer.descriptor, vSerializer.descriptor);
    }

    override collectionSize(collection: Record<K, V>): number {
        return Object.values(collection).length
    }

    override collectionToArray(collection: Record<K, V>): [K, V][] {
        return recordToArray(collection, (k,v) => [k,v])
    }


    builder(): Record<K, V> {
        return {} as Record<K, V>
    }

    builderSize(builder: Record<K, V>): number {
        return Object.values(builder).length
    }

    toResult(builder: Record<K, V>): Record<K, V> {
        return builder;
    }

    toBuilder(collection: Record<K, V>): Record<K, V> {
        return collection
    }

    checkCapacity(builder: Record<K, V>, size: number): void {
    }

}
export class TupleSerializer implements TsSerializer<unknown[]>  {
    private readonly elementSerializers: TsSerializer<unknown>[]; // Define the type of element serializers
     descriptor: SerialDescriptor; // Define the type of descriptor

    constructor(elementSerializers: TsSerializer<unknown>[]) {
        this.elementSerializers = elementSerializers;
        this.descriptor = new TupleDescriptor(elementSerializers.map(serializer => serializer.descriptor));
    }

    deserialize(decoder: Decoder): unknown[] {
        const builder: unknown[] = [];
        const compositeDecoder = decoder.beginStructure(this.descriptor);
        if (compositeDecoder.decodeSequentially()) {
            this.readAll(compositeDecoder, builder, this.readSize(compositeDecoder, builder));
        } else {
            let index: number;
            while ((index = compositeDecoder.decodeElementIndex(this.descriptor)) !== DECODER_DECODE_DONE) {
                this.readElement(compositeDecoder, index, builder);
            }
        }
        compositeDecoder.endStructure(this.descriptor);
        return builder;
    }

    private readAll(decoder: CompositeDecoder, list: unknown[], size: number): void {
        if (size < 0) throw new Error("Size must be known in advance when using READ_ALL");
        for (let index = 0; index < size; index++) {
            this.readElement(decoder, index, list);
        }
    }

    private readElement(decoder: CompositeDecoder, index: number, builder: unknown[]): void {
        builder[index] = decoder.decodeSerializableElement(this.descriptor, index, this.elementSerializers[index]);
    }

    serialize(encoder: Encoder, value: unknown[]): void {
        const size = value.length;
        const collection = encoder.beginCollection(this.descriptor, size)
        value.forEach((item, i) => {
            collection.encodeSerializableElement(this.descriptor, i, this.elementSerializers[i], item);
        });
        collection.endStructure(this.descriptor)
    }

    private readSize(decoder: CompositeDecoder, builder: unknown[]): number {
        const size = decoder.decodeCollectionSize(this.descriptor);
        if (builder.length < size) builder.length = size;
        return size;
    }
}

export const DayjsSerializer: TsSerializer<Dayjs> = {
    descriptor: StringSerializer.descriptor,
    serialize: function (encoder: Encoder, value: Dayjs): void {
        encoder.encodeString(value.toISOString())
    },
    deserialize: function (decoder: Decoder): Dayjs {
        return dayjs(decoder.decodeString())
    }
};

//     override val descriptor: SerialDescriptor = String.serializer().descriptor
//
//     override fun deserialize(decoder: Decoder): Instant = Instant.parse(decoder.decodeString())
//
//     override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeString(value.toString())

export const DurationSerializer: TsSerializer<Duration> = {
    descriptor: StringSerializer.descriptor,
    serialize: function (encoder: Encoder, value: Duration): void {
        encoder.encodeString(value.toISOString())
    },
    deserialize: function (decoder: Decoder): Duration {
        return dayjs.duration(decoder.decodeString())
    }
};

export const VoidSerializer: TsSerializer<void> = {
    descriptor: StringSerializer.descriptor,
    serialize: function (encoder: Encoder, value: void): void {
        encoder.encodeString("void")
    },
    deserialize: function (decoder: Decoder): void {
        decoder.decodeString()
    }
};

export class NullableSerializer<T> implements TsSerializer<T | null> {
    descriptor: SerialDescriptor
    serializer: TsSerializer<T>

    constructor(serializer: TsSerializer<T>) {
        this.descriptor = new NullableSerializerDescriptor(serializer.descriptor)
        this.serializer = serializer
    }

    serialize(encoder: Encoder, value: T | null): void {
        if (value !== null) {
            encoder.encodeNotNullMark();
            encoder.encodeSerializableValue(this.serializer, value);
        } else {
            encoder.encodeNull();
        }
    }

    deserialize(decoder: Decoder): T | null {
        return decoder.decodeNotNullMark() ? decoder.decodeSerializableValue(this.serializer) : decoder.decodeNull();
    }
}