import {SerialDescriptor} from "./SerialDescriptor";
import {SerializationStrategy} from "./TsSerializer";

export interface Encoder {
    serializersModule: any; // Placeholder for SerializersModule

    encodeNotNullMark(): void;

    encodeNull(): void;

    encodeBoolean(value: boolean): void;

    encodeNumber(value: number): void; // Note: TypeScript doesn't have a distinct byte type

    encodeString(value: string): void;

    encodeEnum(enumDescriptor: SerialDescriptor, index: number): void;

    encodeInline(descriptor: SerialDescriptor): Encoder;

    beginStructure(descriptor: SerialDescriptor): CompositeEncoder;

    beginCollection(
        descriptor: SerialDescriptor,
        collectionSize: number
    ): CompositeEncoder;

    encodeSerializableValue<T>(serializer: SerializationStrategy<T>, value: T): void;

    encodeNullableSerializableValue<T>(
        serializer: SerializationStrategy<T>,
        value: T | null
    ): void;
}

export interface CompositeEncoder {
    serializersModule: SerializersModule;

    endStructure(descriptor: SerialDescriptor): void;

    shouldEncodeElementDefault(descriptor: SerialDescriptor, index: number): boolean;

    encodeBooleanElement(descriptor: SerialDescriptor, index: number, value: boolean): void;

    encodeNumberElement(descriptor: SerialDescriptor, index: number, value: number): void;

    encodeStringElement(descriptor: SerialDescriptor, index: number, value: string): void;

    encodeInlineElement(descriptor: SerialDescriptor, index: number): Encoder;

    encodeSerializableElement<T>(
        descriptor: SerialDescriptor,
        index: number,
        serializer: SerializationStrategy<T>,
        value: T
    ): void;

    encodeNullableSerializableElement<T>(
        descriptor: SerialDescriptor,
        index: number,
        serializer: SerializationStrategy<T>,
        value: T | null
    ): void;
}