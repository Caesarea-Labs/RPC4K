/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
import {SerialDescriptor} from "../SerialDescriptor";
import {SerializationStrategy} from "../../TsSerializer";
import {Encoder} from "./Encoder";
import {SerializersModule} from "../SerializersModule";

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

export function encodeStructure(encoder: Encoder, descriptor: SerialDescriptor, block: (composite: CompositeEncoder) => void): void {
    const composite = encoder.beginStructure(descriptor);
    block(composite);
    composite.endStructure(descriptor);
}

export function encodeCollection(encoder: Encoder, descriptor: SerialDescriptor, collectionSize: number, block: (composite: CompositeEncoder) => void): void {
    const composite = encoder.beginCollection(descriptor, collectionSize);
    block(composite);
    composite.endStructure(descriptor);
}

export function encodeCollectionWithElements<E>(encoder: Encoder, descriptor: SerialDescriptor, collection: Array<E>, block: (composite: CompositeEncoder, index: number, element: E) => void): void {
    encodeCollection(encoder, descriptor, collection.length, (composite) => {
        collection.forEach((element, index) => {
            block(composite, index, element);
        });
    });
}