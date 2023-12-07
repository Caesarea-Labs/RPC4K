/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
import {JsonEncoder} from "./JsonEncoder";
import {DeserializationStrategy, SerializationStrategy} from "../../TsSerializer";
import {SerialDescriptor} from "../../core/SerialDescriptor";
import {Json} from "../Json";
import {isPolymorphicKind, isPrimitiveKind, OtherSerialType, PolymorphicKind, PrimitiveKind, SerialKind} from "../../core/SerialKind";
import {AbstractPolymorphicSerializer} from "../../internal/AbstractPolymorphicSerializer";
import {UnionSerializer} from "../../UnionSerializer";
import {JsonDecoder} from "./JsonDecoder";

export function encodePolymorphically<T>(
    encoder: JsonEncoder,
    serializer: SerializationStrategy<T>,
    value: T,
    ifPolymorphic: (discriminator: string) => void
) {
    // serializer.serialize(encoder, value)
    if (!(serializer instanceof AbstractPolymorphicSerializer) || encoder.json.configuration.useArrayPolymorphism) {
        serializer.serialize(encoder, value);
        return;
    }
    const casted = serializer as AbstractPolymorphicSerializer<any>;
    const baseClassDiscriminator = classDiscriminator(serializer.descriptor, encoder.json);
    const actualSerializer = casted.findPolymorphicSerializer(encoder, value);
    validateIfSealed(serializer, actualSerializer, baseClassDiscriminator);
    checkKind(actualSerializer.descriptor.kind);
    ifPolymorphic(baseClassDiscriminator);
    actualSerializer.serialize(encoder, value);
}

function validateIfSealed(
    serializer: SerializationStrategy<any>,
    actualSerializer: SerializationStrategy<any>,
    classDiscriminator: string
) {
    if (!(serializer instanceof UnionSerializer)) return;
    // if (actualSerializer.descriptor.jsonCachedSerialNames().includes(classDiscriminator)) {
    //     const baseName = serializer.descriptor.serialName;
    //     const actualName = actualSerializer.descriptor.serialName;
    //     throw new Error(
    //         `Sealed class '${actualName}' cannot be serialized as base class '${baseName}' because ` +
    //         `it has property name that conflicts with JSON class discriminator '${classDiscriminator}'. ` +
    //         `You can either change class discriminator in JsonConfiguration, ` +
    //         `rename property with @SerialName annotation or fall back to array polymorphism`
    //     );
    // }
}

function checkKind(kind: SerialKind) {
    if (kind === OtherSerialType.ENUM) throw new Error("Enums cannot be serialized polymorphically with 'type' parameter. You can use 'JsonBuilder.useArrayPolymorphism' instead");
    if (isPrimitiveKind(kind)) throw new Error("Primitives cannot be serialized polymorphically with 'type' parameter. You can use 'JsonBuilder.useArrayPolymorphism' instead");
    if (isPolymorphicKind(kind)) throw new Error("Actual serializer for polymorphic cannot be polymorphic itself");
}

// function decodeSerializableValuePolymorphic<T>(
//     decoder: JsonDecoder,
//     deserializer: DeserializationStrategy<T>
// ): T {
//     if (!(deserializer instanceof AbstractPolymorphicSerializer) || decoder.json.configuration.useArrayPolymorphism) {
//         return deserializer.deserialize(decoder);
//     }
//     const discriminator = classDiscriminator(deserializer.descriptor, decoder.json);
//
//     const jsonTree = cast<JsonObject>(decoder.decodeJsonElement(), deserializer.descriptor);
//     const type = jsonTree[discriminator]?.jsonPrimitive?.content;
//     const actualSerializer = deserializer.findPolymorphicSerializerOrNull(decoder, type)
//         || throwSerializerNotFound(type, jsonTree);
//
//     return decoder.json.readPolymorphicJson(discriminator, jsonTree, actualSerializer);
// }

// function throwSerializerNotFound(type: string | null, jsonTree: JsonObject): never {
//     const suffix = type == null ? "missing class discriminator ('null')" : `class discriminator '${type}'`;
//     throw new JsonDecodingException(-1, `Polymorphic serializer was not found for ${suffix}`, jsonTree.toString());
// }

export function classDiscriminator(descriptor: SerialDescriptor, json: Json): string {
    // for (const annotation of descriptor.annotations) {
    //     if (annotation instanceof JsonClassDiscriminator) return annotation.discriminator;
    // }
    return json.configuration.classDiscriminator;
}