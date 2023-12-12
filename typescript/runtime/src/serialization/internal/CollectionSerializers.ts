import {Encoder} from "../core/encoding/Encoder";
import {CompositeDecoder, Decoder, DECODER_DECODE_DONE} from "../core/encoding/Decoding";
import {TsSerializer} from "../TsSerializer";
import {SerialDescriptor} from "../core/SerialDescriptor";
import {encodeCollection} from "../core/encoding/Encoding";
import {isPrimitiveKind} from "../core/SerialKind";
import {RecordDesc} from "./CollectionDescriptors";
import {recordForEach} from "ts-minimum";

abstract class AbstractCollectionSerializer<Element, Collection, Builder> implements TsSerializer<Collection> {
    abstract descriptor: SerialDescriptor
    abstract collectionSize(collection: Collection): number;
    abstract collectionToArray(collection: Collection): Element[];
    // abstract collectionIterator(collection: Collection): Iterator<Element>;
    abstract builder(): Builder;
    abstract builderSize(builder: Builder): number;
    abstract toResult(builder: Builder): Collection;
    abstract toBuilder(collection: Collection): Builder;
    abstract checkCapacity(builder: Builder, size: number): void;
    abstract serialize(encoder: Encoder, value: Collection): void;

    //
    public merge(decoder: Decoder, previous: Collection | null): Collection {
        const builder = previous !== null ? this.toBuilder(previous) : this.builder();
        const startIndex = this.builderSize(builder);
        const compositeDecoder = decoder.beginStructure(this.descriptor);
        if (compositeDecoder.decodeSequentially()) {
            this.readAll(compositeDecoder, builder, startIndex, this.readSize(compositeDecoder, builder));
        } else {
            while (true) {
                const index = compositeDecoder.decodeElementIndex(this.descriptor);
                if (index === DECODER_DECODE_DONE) break;
                this.readElement(compositeDecoder, startIndex + index, builder);
            }
        }
        compositeDecoder.endStructure(this.descriptor);
        return this.toResult(builder);
    }

    public deserialize(decoder: Decoder): Collection {
        return this.merge(decoder, null);
    }

    private readSize(decoder: CompositeDecoder, builder: Builder): number {
        const size = decoder.decodeCollectionSize(this.descriptor);
        this.checkCapacity(builder, size);
        return size;
    }

    abstract readElement(decoder: CompositeDecoder, index: number, builder: Builder, checkIndex?: boolean): void;
    abstract readAll(decoder: CompositeDecoder, builder: Builder, startIndex: number, size: number): void;
}

abstract class CollectionLikeSerializer<Element, Collection, Builder> extends AbstractCollectionSerializer<Element, Collection, Builder> {
     elementSerializer: TsSerializer<Element>;

    constructor(elementSerializer: TsSerializer<Element>) {
        super();
        this.elementSerializer = elementSerializer;
    }

    protected abstract insert(builder: Builder, index: number, element: Element): void;
    abstract get descriptor(): SerialDescriptor;

    public serialize(encoder: Encoder, value: Collection): void {
        const size = this.collectionSize(value);
        encodeCollection(encoder,this.descriptor, size, (composite) => {
            this.collectionToArray(value).forEach((element, index) => {
                composite.encodeSerializableElement(this.descriptor, index, this.elementSerializer, element);
            })
        });
    }

    public readAll(decoder: CompositeDecoder, builder: Builder, startIndex: number, size: number): void {
        if (size < 0) {
            throw new Error("Size must be known in advance when using READ_ALL");
        }
        for (let index = 0; index < size; index++) {
            this.readElement(decoder, startIndex + index, builder, false);
        }
    }

    public readElement(decoder: CompositeDecoder, index: number, builder: Builder, checkIndex: boolean): void {
        this.insert(builder, index, decoder.decodeSerializableElement(this.descriptor, index, this.elementSerializer));
    }
}

export abstract class CollectionSerializer<E, C extends E[], B> extends CollectionLikeSerializer<E, C, B> {
    constructor(element: TsSerializer<E>) {
        super(element);
    }

     collectionSize(collection: C): number {
        return collection.length;
    }

     collectionToArray(collection: C): E[] {
        return collection
    }
    //adapted to return an array with collectionToArray instead
    // protected collectionIterator(collection: C): Iterator<E> {
    //     return collection.iterator();
    // }
}


export abstract class MapLikeSerializer<Key extends string | number, Value, Builder extends Record<Key, Value>> extends AbstractCollectionSerializer<[Key,Value], Record<Key,Value>, Builder>{
    protected keySerializer: TsSerializer<Key>;
    protected valueSerializer: TsSerializer<Value>;

    protected constructor(keySerializer: TsSerializer<Key>, valueSerializer: TsSerializer<Value>) {
        super()
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
    }

    abstract get descriptor(): SerialDescriptor;

     override readAll(decoder: CompositeDecoder, builder: Builder, startIndex: number, size: number): void {
        if (size < 0) {
            throw new Error("Size must be known in advance when using READ_ALL");
        }
        for (let index = 0; index < size * 2; index += 2) {
            this.readElement(decoder, startIndex + index, builder, false);
        }
    }

    override readElement(decoder: CompositeDecoder, index: number, builder: Builder, checkIndex: boolean = true): void {
        const key: Key = decoder.decodeSerializableElement(this.descriptor, index, this.keySerializer);
        let vIndex: number;
        if (checkIndex) {
            vIndex = decoder.decodeElementIndex(this.descriptor);
            if (vIndex !== index + 1) {
                throw new Error(`Value must follow key in a map, index for key: ${index}, returned index for value: ${vIndex}`);
            }
        } else {
            vIndex = index + 1;
        }
        const value = (key in builder && !isPrimitiveKind(this.valueSerializer.descriptor.kind)
            ? decoder.decodeSerializableElement(this.descriptor, vIndex, this.valueSerializer, builder[key])
            : decoder.decodeSerializableElement(this.descriptor, vIndex, this.valueSerializer)) as Builder[Key]

        builder[key] = value;
    }

    override serialize(encoder: Encoder, value: Record<Key, Value>): void {
        const size = Object.values(value).length;
        const collection = encoder.beginCollection(this.descriptor,size)
        let index = 0;
        recordForEach(value, (k,v) => {
            collection.encodeSerializableElement(this.descriptor, index++, this.keySerializer, k);
            collection.encodeSerializableElement(this.descriptor, index++, this.valueSerializer, v);
        })
        collection.endStructure(this.descriptor)
    }

    // Additional methods and helper functions may be needed.
}

