import {Encoder} from "../core/encoding/Encoder";
import {CompositeDecoder, Decoder, DECODER_DECODE_DONE} from "../core/encoding/Decoding";
import {TsSerializer} from "../TsSerializer";
import {SerialDescriptor} from "../core/SerialDescriptor";
import {encodeCollection} from "../core/encoding/Encoding";

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

    // TypeScript does not support internal annotations like Kotlin's @InternalSerializationApi
    public merge(decoder: Decoder, previous: Collection | null): Collection {
        const builder = previous ? this.toBuilder(previous) : this.builder();
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

