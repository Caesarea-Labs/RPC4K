/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
import {CompositeDecoder, Decoder} from "./Decoding";
import {SerializationException} from "../SerializationException";
import {SerialDescriptor} from "../SerialDescriptor";
import {DeserializationStrategy} from "../../TsSerializer";

export abstract class AbstractDecoder extends Decoder implements CompositeDecoder {
    abstract decodeElementIndex(descriptor: SerialDescriptor): number

    decodeSequentially(): boolean {
        return false
    }

    decodeCollectionSize(descriptor: SerialDescriptor): number {
        return -1
    }


    /**
     * Invoked to decode a value when specialized `decode*` method was not overridden.
     */
    public decodeValue(): any {
        throw new SerializationException(`${this.constructor.name} can't retrieve untyped values`);
    }

    public decodeNotNullMark(): boolean {
        return true;
    }

    public decodeNull(): any {
        return null;
    }

    public decodeBoolean(): boolean {
        return this.decodeValue() as boolean;
    }

    public decodeNumber(): number {
        return this.decodeValue() as number;
    }

    public decodeString(): string {
        return this.decodeValue() as string;
    }

    public decodeEnum(enumDescriptor: SerialDescriptor): number {
        return this.decodeValue() as number;
    }

    public decodeInline(descriptor: SerialDescriptor): Decoder {
        return this;
    }

    public decodeSerializableValue<T = any>(deserializer: DeserializationStrategy<T>, previousValue?: T | null): T {
        return this.decodeValue() as T;
    }

    public beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        return this;
    }

    public endStructure(descriptor: SerialDescriptor): void {
    }

    public decodeBooleanElement(descriptor: SerialDescriptor, index: number): boolean {
        return this.decodeBoolean();
    }

    public decodeNumberElement(descriptor: SerialDescriptor, index: number): number {
        return this.decodeNumber();
    }

    decodeStringElement(descriptor: SerialDescriptor, index: number): string {
        return this.decodeString()
    }


    public decodeInlineElement(descriptor: SerialDescriptor, index: number): Decoder {
        return this.decodeInline(descriptor.getElementDescriptor(index));
    }

    public decodeSerializableElement<T>(
        descriptor: SerialDescriptor,
        index: number,
        deserializer: DeserializationStrategy<T>,
        previousValue?: T | null
    ): T {
        return this.decodeSerializableValue<T>(deserializer, previousValue);
    }

    public decodeNullableSerializableElement<T>(
        descriptor: SerialDescriptor,
        index: number,
        deserializer: DeserializationStrategy<T | null>,
        previousValue?: T
    ): T | null {
        const isNullabilitySupported = deserializer.descriptor.isNullable;
        if (isNullabilitySupported || this.decodeNotNullMark()) {
            return this.decodeSerializableValue(deserializer, previousValue);
        } else {
            return this.decodeNull();
        }
    }
}
