import {CompositeEncoder, Encoder} from "./Encoder";
import {SerialDescriptor} from "./SerialDescriptor";
import {SerializationStrategy} from "./TsSerializer";

abstract class AbstractEncoder implements Encoder, CompositeEncoder {
    beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        return this as unknown as CompositeEncoder;
    }

    endStructure(descriptor: SerialDescriptor): void {}

    /**
     * Invoked before writing an element that is part of the structure to determine whether it should be encoded.
     * Element information can be obtained from the [descriptor] by the given [index].
     *
     * @return `true` if the value should be encoded, false otherwise
     */
    encodeElement(descriptor: SerialDescriptor, index: number): boolean {
        return true;
    }

    /**
     * Invoked to encode a value when specialized `encode*` method was not overridden.
     */
    encodeValue(value: unknown): void {
        throw new SerializationException(`Non-serializable ${typeof value} is not supported by ${this.constructor.name} encoder`);
    }

    encodeNull(): void {
        throw new SerializationException("'null' is not supported by default");
    }

    encodeBoolean(value: boolean): void {
        this.encodeValue(value);
    }

    encodeNumber(value: number) {
        this.encodeValue(value)
    }

    encodeString(value: string): void {
        this.encodeValue(value);
    }

    encodeEnum(enumDescriptor: SerialDescriptor, index: number): void {
        this.encodeValue(index);
    }

    encodeInline(descriptor: SerialDescriptor): Encoder {
        return this as unknown as Encoder;
    }

    abstract serializersModule: SerializersModule;

    abstract beginCollection(descriptor: SerialDescriptor, collectionSize: number): CompositeEncoder;

    abstract encodeBooleanElement(descriptor: SerialDescriptor, index: number, value: boolean): void;

    abstract encodeInlineElement(descriptor: SerialDescriptor, index: number): Encoder

    abstract encodeNotNullMark(): void

    abstract encodeNullableSerializableElement<T>(descriptor: SerialDescriptor, index: number, serializer: SerializationStrategy<T>, value: T | null): void

    abstract encodeNullableSerializableValue<T>(serializer: SerializationStrategy<T>, value: T | null): void

    abstract encodeNumberElement(descriptor: SerialDescriptor, index: number, value: number): void

    abstract encodeSerializableElement<T>(descriptor: SerialDescriptor, index: number, serializer: SerializationStrategy<T>, value: T): void

    abstract encodeSerializableValue<T>(serializer: SerializationStrategy<T>, value: T): void

    abstract encodeStringElement(descriptor: SerialDescriptor, index: number, value: string): void

    abstract shouldEncodeElementDefault(descriptor: SerialDescriptor, index: number): boolean
}