import {CompositeDecoder, Decoder} from "../core/encoding/Decoding";
import {CompositeEncoder} from "../core/encoding/Encoding";
import {TsSerializer} from "../TsSerializer";
import {SerialDescriptor} from "../core/SerialDescriptor";
import {ListLikeDescriptor} from "./CollectionDescriptors";
import {Encoder} from "../core/encoding/Encoder";
import {CollectionLikeSerializer} from "./CollectionSerializers";

export abstract class PrimitiveArraySerializer<Element, Array, Builder extends PrimitiveArrayBuilder<Array>> extends CollectionLikeSerializer<Element,Array, Builder>{
    public descriptor: SerialDescriptor
     constructor(private primitiveSerializer: TsSerializer<Element>) {
        super(primitiveSerializer)
        this.descriptor = new PrimitiveArrayDescriptor(
            this.primitiveSerializer.descriptor
        );
    }

    builderSize(builder: Builder): number {
        return builder.position
    }

    toResult(builder: Builder): Array {
        return builder.build()
    }

    checkCapacity(builder: Builder, size: number) {
        builder.ensureCapacity(size)
    }
    collectionToArray(collection: Array): Element[] {
        throw new Error("This method lead to boxing and must not be used, use writeContents instead")
    }

    protected insert(builder: Builder, index: number, element: Element) {
        throw new Error("This method lead to boxing and must not be used, use append(Builder) instead")
    }

    builder(): Builder {
        return this.toBuilder(this.empty())
    }


    public abstract empty(): Array;

    public abstract writeContent(
        encoder: CompositeEncoder,
        content: Array,
        size: number
    ): void;

    public abstract readElement(
        decoder: CompositeDecoder,
        index: number,
        builder: Builder,
        checkIndex: boolean
    ): void;


    serialize(encoder: Encoder, value: Array): void {
        const size: number = this.collectionSize(value)
        const cEncoder = encoder.beginCollection(this.descriptor,size)
        this.writeContent(cEncoder,value,size)
        cEncoder.endStructure(this.descriptor)
    }

    deserialize(decoder: Decoder): Array {
        return this.merge(decoder, null);
    }
}

 class PrimitiveArrayDescriptor extends ListLikeDescriptor {
    constructor(primitive: SerialDescriptor) {
        super(primitive);
    }
    serialName = `${this.elementDescriptor.serialName}Array`
 }



 abstract class PrimitiveArrayBuilder<Array> {
    abstract position: number
    abstract ensureCapacity(requiredCapacity: number): void
    abstract build(): Array
 }

const INITIAL_SIZE = 10;

export class ByteArrayBuilder extends PrimitiveArrayBuilder<Int8Array>{
    private buffer: Int8Array;
    public position: number;

    public constructor(bufferWithData: Int8Array) {
        super()
        this.buffer = bufferWithData;
        this.position = bufferWithData.length;
        this.ensureCapacity(INITIAL_SIZE);
    }

    public ensureCapacity(requiredCapacity: number = this.position + 1): void {
        if (this.buffer.length < requiredCapacity) {
            let newSize = Math.max(requiredCapacity, this.buffer.length * 2);
            let newBuffer = new Int8Array(newSize);
            newBuffer.set(this.buffer);
            this.buffer = newBuffer;
        }
    }

    public append(c: number): void { // TypeScript does not have a Byte type, using number instead
        this.ensureCapacity();
        this.buffer[this.position++] = c;
    }

    public build(): Int8Array {
        return this.buffer.slice(0, this.position);
    }
}

export class UByteArrayBuilder extends PrimitiveArrayBuilder<Uint8Array>{
    private buffer: Uint8Array;
    public position: number;

    public constructor(bufferWithData: Uint8Array) {
        super()
        this.buffer = bufferWithData;
        this.position = bufferWithData.length;
        this.ensureCapacity(INITIAL_SIZE);
    }

    public ensureCapacity(requiredCapacity: number = this.position + 1): void {
        if (this.buffer.length < requiredCapacity) {
            let newSize = Math.max(requiredCapacity, this.buffer.length * 2);
            let newBuffer = new Uint8Array(newSize);
            newBuffer.set(this.buffer);
            this.buffer = newBuffer;
        }
    }

    public append(c: number): void { // TypeScript does not have a Byte type, using number instead
        this.ensureCapacity();
        this.buffer[this.position++] = c;
    }

    public build(): Uint8Array {
        return this.buffer.slice(0, this.position);
    }
}

//


// internal constructor(
//     primitive: SerialDescriptor
// ) : ListLikeDescriptor(primitive) {
//     override val serialName: String = "${primitive.serialName}Array"
// }