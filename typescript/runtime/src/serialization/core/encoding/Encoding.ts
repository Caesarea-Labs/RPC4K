/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
import {SerialDescriptor} from "../SerialDescriptor";
import {SerializationStrategy} from "../TsSerializer";
import {Encoder} from "./Encoder";

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