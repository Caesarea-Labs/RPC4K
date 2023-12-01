/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
import {SerialDescriptor} from "../SerialDescriptor";
import {DeserializationStrategy} from "../TsSerializer";
import {SerializersModule} from "../SerializersModule";

export abstract class Decoder {
    // Context of the current serialization process, including contextual and polymorphic serialization and,
    // potentially, a format-specific configuration.
    abstract serializersModule: SerializersModule;

    // Returns `true` if the current value in decoder is not null, false otherwise.
    abstract decodeNotNullMark(): boolean;

    // Decodes the `null` value and returns it.
    abstract decodeNull(): null;

    // Decodes a boolean value.
    abstract decodeBoolean(): boolean;

    abstract decodeNumber(): number;

    // Decodes a string value.
    abstract decodeString(): string;

    // Decodes a enum value and returns its index in elements collection.
    abstract decodeEnum(enumDescriptor: SerialDescriptor): number;

    // Returns Decoder for decoding an underlying type of a value class in an inline manner.
    abstract decodeInline(descriptor: SerialDescriptor): Decoder;

    // Decodes the beginning of the nested structure in a serialized form
    // and returns CompositeDecoder responsible for decoding this very structure.
    abstract beginStructure(descriptor: SerialDescriptor): CompositeDecoder;

    // Decodes the value of type T by delegating the decoding process to the given deserializer.
    decodeSerializableValue<T>(deserializer: DeserializationStrategy<T>): T {
        return deserializer.deserialize(this)
    }

    // Decodes the nullable value of type T by delegating the decoding process to the given deserializer.
    decodeNullableSerializableValue<T>(deserializer: DeserializationStrategy<T | null>): T | null {
        const isNullabilitySupported = deserializer.descriptor.isNullable
        return (isNullabilitySupported || this.decodeNotNullMark()) ? this.decodeSerializableValue(deserializer) : this.decodeNull()
    }
}

// Static constants
export const DECODER_DECODE_DONE: number = -1;
export const DECODER_UNKNOWN_NAME: number = -3;

export interface CompositeDecoder {
    // Property signatures
    serializersModule: SerializersModule;

    // Method signatures
    endStructure(descriptor: SerialDescriptor): void;

    decodeSequentially(): boolean

    decodeElementIndex(descriptor: SerialDescriptor): number;

    // Default: return -1
    decodeCollectionSize(descriptor: SerialDescriptor): number

    decodeBooleanElement(descriptor: SerialDescriptor, index: number): boolean;

    decodeNumberElement(descriptor: SerialDescriptor, index: number): number;

    decodeStringElement(descriptor: SerialDescriptor, index: number): string;

    decodeInlineElement(descriptor: SerialDescriptor, index: number): Decoder;

    decodeSerializableElement<T>(descriptor: SerialDescriptor, index: number, deserializer: DeserializationStrategy<T>, previousValue?: T | null): T;

    decodeNullableSerializableElement<T>(descriptor: SerialDescriptor, index: number, deserializer: DeserializationStrategy<T | null>, previousValue?: T | null): T | null;

}
