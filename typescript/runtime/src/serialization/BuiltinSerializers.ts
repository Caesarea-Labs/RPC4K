/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
import {PrimitiveKind} from "./core/SerialKind";
import {TsSerializer} from "./TsSerializer";
import {Encoder} from "./core/encoding/Encoder";
import {Decoder} from "./core/encoding/Decoding";
import {PrimitiveSerialDescriptor} from "./builtins/PrimitiveSerialDescriptor";
import {SerialDescriptor} from "./core/SerialDescriptor";
import {ArrayDesc} from "./internal/CollectionDescriptors";
import {CollectionSerializer} from "./internal/CollectionSerializers";
import {Dayjs} from "dayjs";
import {Duration} from "dayjs/plugin/duration";
import {NullableSerializerDescriptor} from "./builtins/NullableSerializerDescriptor";

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

const todoDescriptor: SerialDescriptor  =new PrimitiveSerialDescriptor("TODO", PrimitiveKind.NUMBER)

export class RecordSerializer<K extends string | number,V> implements TsSerializer<Record<K,V>>  {
    constructor(keySerializer: TsSerializer<any>, valueSerializer: TsSerializer<any>) {
    }
    serialize(encoder: Encoder, value: Record<K, V>): void {
        throw new Error("Method not implemented.");
    }
    deserialize(decoder: Decoder): Record<K, V> {
        throw new Error("Method not implemented.");
    }
    descriptor= todoDescriptor
}
export class HeterogeneousArraySerializer implements TsSerializer<unknown[]>  {
    constructor(serializers: TsSerializer<any>[]) {
    }
    serialize(encoder: Encoder, value: unknown[]): void {
        throw new Error("Method not implemented.");
    }
    deserialize(decoder: Decoder): unknown[] {
        throw new Error("Method not implemented.");
    }
    descriptor= todoDescriptor
};

export const DayjsSerializer: TsSerializer<Dayjs> = {
    descriptor: todoDescriptor,
    serialize: function (encoder: Encoder, value: Dayjs): void {
        throw new Error("Function not implemented.");
    },
    deserialize: function (decoder: Decoder): Dayjs {
        throw new Error("Function not implemented.");
    }
};

export const DurationSerializer: TsSerializer<Duration> = {
    descriptor: todoDescriptor,
    serialize: function (encoder: Encoder, value: Duration): void {
        throw new Error("Function not implemented.");
    },
    deserialize: function (decoder: Decoder): Duration {
        throw new Error("Function not implemented.");
    }
};

export const VoidSerializer: TsSerializer<void> = {
    descriptor: todoDescriptor,
    serialize: function (encoder: Encoder, value: void): void {
        throw new Error("Function not implemented.");
    },
    deserialize: function (decoder: Decoder): void {
        throw new Error("Function not implemented.");
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