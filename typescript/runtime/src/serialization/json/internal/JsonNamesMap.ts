/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
import {Json} from "../Json";
import {SerialDescriptor} from "../../core/SerialDescriptor";
import {DECODER_UNKNOWN_NAME} from "../../core/encoding/Decoding";
import {OtherSerialType, StructureKind} from "../../core/SerialKind";
import {JsonException} from "./JsonExceptions";
import {JsonNamingStrategy} from "../JsonConfiguration";

// TODO: dont delete
// Assuming DescriptorSchemaCache and Key types are defined
// const JsonDeserializationNamesKey = new DescriptorSchemaCache.Key<Map<string, number>>();
// const JsonSerializationNamesKey = new DescriptorSchemaCache.Key<Array<string>>();
//
// function buildDeserializationNamesMap(serialDescriptor: SerialDescriptor, json: Json): Map<string, number> {
//     const builder: Map<string, number> = new Map();
//
//     const putOrThrow = (name: string, index: number) => {
//         const entity = serialDescriptor.kind === SerialKind.ENUM ? "enum value" : "property";
//         if (builder.has(name)) {
//             throw new JsonException(
//                 `The suggested name '${name}' for ${entity} ${serialDescriptor.getElementName(index)} is already one of the names for ${entity} ` +
//                 `${serialDescriptor.getElementName(builder.get(name)!)} in ${serialDescriptor}`
//             );
//         }
//         builder.set(name, index);
//     };
//
//     const useLowercaseEnums = decodeCaseInsensitive(json,serialDescriptor)
//     const strategyForClasses = namingStrategy(json, serialDescriptor);
//
//     for (let i = 0; i < serialDescriptor.elementsCount; i++) {
//         // // Assuming getElementAnnotations and JsonNames types are defined
//         // serialDescriptor.getElementAnnotations(i).filter(annotation => annotation instanceof JsonNames).forEach(annotation => {
//         //     annotation.names.forEach(name => {
//         //         putOrThrow(useLowercaseEnums ? name.toLowerCase() : name, i);
//         //     });
//         // });
//
//         let nameToPut: string | null = null;
//         if (useLowercaseEnums) {
//             nameToPut = serialDescriptor.getElementName(i).toLowerCase();
//         } else if (strategyForClasses !== null) {
//             nameToPut = strategyForClasses.serialNameForJson(serialDescriptor, i, serialDescriptor.getElementName(i));
//         }
//
//         if (nameToPut !== null) {
//             putOrThrow(nameToPut, i);
//         }
//     }
//
//     return builder.size === 0 ? new Map() : builder;
// }
//
// function deserializationNamesMap(json: Json, descriptor: SerialDescriptor): Map<string, number> {
//     return json.schemaCache.getOrPut(descriptor, JsonDeserializationNamesKey, () => buildDeserializationNamesMap(descriptor, json));
// }

// function serializationNamesIndices(serialDescriptor: SerialDescriptor, json: Json, strategy: JsonNamingStrategy): Array<string> {
//     return json.schemaCache.getOrPut(serialDescriptor, JsonSerializationNamesKey, () => {
//         return Array.from({ length: serialDescriptor.elementsCount }, (_, i) => {
//             const baseName = serialDescriptor.getElementName(i);
//             return strategy.serialNameForJson(serialDescriptor, i, baseName);
//         });
//     });
// }

export function getJsonElementName(serialDescriptor: SerialDescriptor, json: Json, index: number): string {
    return serialDescriptor.getElementName(index)
    //TODO: do
    // const strategy = namingStrategy(json, serialDescriptor);
    // return strategy === null ? serialDescriptor.getElementName(index) : serializationNamesIndices(serialDescriptor, json, strategy)[index];
}
export function getJsonNameIndexOrThrow(serialDescriptor: SerialDescriptor, json: Json,  name: string , suffix: String = "") : number {
    return getJsonNameIndex(serialDescriptor, json, name)
}

function namingStrategy(json: Json, serialDescriptor: SerialDescriptor) {
    return serialDescriptor.kind === StructureKind.CLASS ? json.configuration.namingStrategy : null;
}

// function getJsonNameIndexSlowPath(serialDescriptor: SerialDescriptor, json: Json, name: string): number {
//     const namesMap = deserializationNamesMap(json, serialDescriptor);
//     return namesMap.get(name) ?? DECODER_UNKNOWN_NAME;
// }

function decodeCaseInsensitive(json: Json, descriptor: SerialDescriptor): boolean {
    return json.configuration.decodeEnumsCaseInsensitive && descriptor.kind === OtherSerialType.ENUM;
}




export function tryCoerceValue(
    json: Json, // Replace 'any' with the actual type of your Json object
    elementDescriptor: SerialDescriptor, // Replace with the actual type of SerialDescriptor
    peekNull: (consume: boolean) => boolean,
    peekString: () => string | null,
    onEnumCoercing: () => void = () => {}
): boolean {
    if (!elementDescriptor.isNullable && peekNull(true)) {
        return true;
    }
    if (elementDescriptor.kind === OtherSerialType.ENUM) {
        if (elementDescriptor.isNullable && peekNull(false)) {
            return false;
        }

        const enumValue = peekString();
        if (enumValue === null) {
            return false; // If value is not a string, decodeEnum() will throw correct exception
        }
        const enumIndex =  getJsonNameIndex(elementDescriptor,json,enumValue)
        if (enumIndex === DECODER_UNKNOWN_NAME) {
            onEnumCoercing();
            return true;
        }
    }
    return false;
}


/**
 * Serves the same purpose as SerialDescriptor.getElementIndex but respects JsonNames annotation
 * and JsonConfiguration settings.
 */
export function getJsonNameIndex(
    serialDescriptor: SerialDescriptor, // Replace with the actual type of SerialDescriptor
    json: Json, // Replace 'Json' with the actual type of your Json object
    name: string
): number {
    return serialDescriptor.getElementIndex(name);
    // TODO: dont delete
    // if (decodeCaseInsensitive(json,serialDescriptor)) {
    //     return getJsonNameIndexSlowPath(serialDescriptor, json, name.toLowerCase());
    // }
    //
    // const strategy = namingStrategy(json, serialDescriptor);
    // if (strategy !== null) return getJsonNameIndexSlowPath(serialDescriptor, json, name);
    // const index = serialDescriptor.getElementIndex(name);
    // Fast path, do not go through ConcurrentHashMap.get
    // if (index !== DECODER_UNKNOWN_NAME) return index;
    // if (!json.configuration.useAlternativeNames) return index;
    // // Slow path
    // return getJsonNameIndexSlowPath(serialDescriptor, json, name);
}