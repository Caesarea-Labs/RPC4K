/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
import {SerialDescriptor} from "../SerialDescriptor";
import {SerializationStrategy} from "../TsSerializer";
import {CompositeEncoder} from "./Encoding";


export abstract class Encoder {
    abstract serializersModule: SerializersModule;

    encodeNotNullMark(): void {

    }

    abstract encodeNull(): void;

    abstract encodeBoolean(value: boolean): void;

    abstract encodeNumber(value: number): void;

    abstract encodeString(value: string): void;

    abstract encodeEnum(enumDescriptor: SerialDescriptor, index: number): void;

    abstract encodeInline(descriptor: SerialDescriptor): Encoder;

    abstract beginStructure(descriptor: SerialDescriptor): CompositeEncoder;

    /**
     * Encodes the beginning of the collection with size collectionSize and the given serializer of its type parameters.
     * This method has to be implemented only if you need to know collection size in advance, otherwise, beginStructure can be used.
     */
    public beginCollection(descriptor: SerialDescriptor, collectionSize: number): CompositeEncoder {
        return this.beginStructure(descriptor);
    }

    /**
     * Encodes the value of type T by delegating the encoding process to the given serializer.
     * For example, encodeInt call us equivalent to delegating integer encoding to Int.serializer:
     * encodeSerializableValue(Int.serializer())
     */
    public encodeSerializableValue<T>(serializer: SerializationStrategy<T>, value: T): void {
        serializer.serialize(this, value);
    }

    /**
     * Encodes the nullable value of type T by delegating the encoding process to the given serializer.
     */
    public encodeNullableSerializableValue<T>(serializer: SerializationStrategy<T>, value: T | null): void {
        const isNullabilitySupported = serializer.descriptor.isNullable;
        if (isNullabilitySupported) {
            // Instead of serializer.serialize to be able to intercept this
            return this.encodeSerializableValue(serializer as SerializationStrategy<T | null>, value);
        }

        // Else default path used to avoid allocation of NullableSerializer
        if (value === null) {
            this.encodeNull();
        } else {
            this.encodeNotNullMark();
            this.encodeSerializableValue(serializer, value);
        }
    }
}

