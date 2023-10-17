import {SerialDescriptor} from "./SerialDescriptor";
import {DeserializationStrategy} from "./TsSerializer";

export interface Decoder {
    // Context of the current serialization process, including contextual and polymorphic serialization and,
    // potentially, a format-specific configuration.
    serializersModule: SerializersModule;

    // Returns `true` if the current value in decoder is not null, false otherwise.
    decodeNotNullMark(): boolean;

    // Decodes the `null` value and returns it.
    decodeNull(): null;

    // Decodes a boolean value.
    decodeBoolean(): boolean;

    decodeNumber(): number;

    // Decodes a string value.
    decodeString(): string;

    // Decodes a enum value and returns its index in elements collection.
    decodeEnum(enumDescriptor: SerialDescriptor): number;

    // Returns Decoder for decoding an underlying type of a value class in an inline manner.
    decodeInline(descriptor: SerialDescriptor): Decoder;

    // Decodes the beginning of the nested structure in a serialized form
    // and returns CompositeDecoder responsible for decoding this very structure.
    beginStructure(descriptor: SerialDescriptor): CompositeDecoder;

    // Decodes the value of type T by delegating the decoding process to the given deserializer.
    decodeSerializableValue<T>(deserializer: DeserializationStrategy<T>): T;

    // Decodes the nullable value of type T by delegating the decoding process to the given deserializer.
    decodeNullableSerializableValue<T>(deserializer: DeserializationStrategy<T | null>): T | null;
}

// Static constants
const DECODER_DECODE_DONE: number = -1;
const DECODER_UNKNOWN_NAME: number = -3;

export interface CompositeDecoder {
    // Property signatures
    serializersModule: SerializersModule;

    // Method signatures
    endStructure(descriptor: SerialDescriptor): void;

    decodeSequentially(): boolean

    decodeElementIndex(descriptor: SerialDescriptor): number;

    decodeCollectionSize(descriptor: SerialDescriptor): number

    decodeBooleanElement(descriptor: SerialDescriptor, index: number): boolean;

    decodeByteElement(descriptor: SerialDescriptor, index: number): number; // Assuming byte is represented as number in TypeScript

    decodeCharElement(descriptor: SerialDescriptor, index: number): string; // Assuming char is represented as a single character string in TypeScript

    decodeShortElement(descriptor: SerialDescriptor, index: number): number;

    decodeIntElement(descriptor: SerialDescriptor, index: number): number;

    decodeLongElement(descriptor: SerialDescriptor, index: number): number; // Note: JavaScript doesn't have a native "long" type

    decodeFloatElement(descriptor: SerialDescriptor, index: number): number;

    decodeDoubleElement(descriptor: SerialDescriptor, index: number): number;

    decodeStringElement(descriptor: SerialDescriptor, index: number): string;

    decodeInlineElement(descriptor: SerialDescriptor, index: number): Decoder;

    decodeSerializableElement<T>(descriptor: SerialDescriptor, index: number, deserializer: DeserializationStrategy<T>, previousValue?: T | null): T;

    decodeNullableSerializableElement<T>(descriptor: SerialDescriptor, index: number, deserializer: DeserializationStrategy<T | null>, previousValue?: T | null): T | null;

}
