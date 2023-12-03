/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
import {Encoder} from "./Encoder";
import {SerialDescriptor} from "../SerialDescriptor";
import {SerializationStrategy} from "../../TsSerializer";
import {CompositeEncoder} from "./Encoding";
import {SerializationException} from "../SerializationException";

export abstract class AbstractEncoder extends Encoder implements CompositeEncoder {
    shouldEncodeElementDefault(descriptor: SerialDescriptor, index: number): boolean {
        return true
    }
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

    public encodeBooleanElement(descriptor: SerialDescriptor, index: number, value: boolean): void {
        if (this.encodeElement(descriptor, index)) {
            this.encodeBoolean(value);
        }
    }

    public encodeNumberElement(descriptor: SerialDescriptor, index: number, value: number): void {
        if (this.encodeElement(descriptor, index)) {
            this.encodeNumber(value);
        }
    }

    public encodeStringElement(descriptor: SerialDescriptor, index: number, value: string): void {
        if (this.encodeElement(descriptor, index)) {
            this.encodeString(value);
        }
    }

    public encodeInlineElement(descriptor: SerialDescriptor, index: number): Encoder {
        if(this.encodeElement(descriptor,index)) {
            return this.encodeInline(descriptor.getElementDescriptor(index))
        } else {
            throw new Error("Not implemented")
            // return NoOpEncoder
        }
    }

    public encodeSerializableElement<T>(
        descriptor: SerialDescriptor,
        index: number,
        serializer: SerializationStrategy<T>,
        value: T
    ): void {
        if (this.encodeElement(descriptor, index)) {
            this.encodeSerializableValue(serializer, value);
        }
    }

    public encodeNullableSerializableElement<T>(
        descriptor: SerialDescriptor,
        index: number,
        serializer: SerializationStrategy<T>,
        value: T | null
    ): void {
        if (this.encodeElement(descriptor, index)) {
            this.encodeNullableSerializableValue(serializer, value);
        }
    }
}
