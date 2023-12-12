/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
import {Json} from "../Json";
import {SerialDescriptor} from "../../core/SerialDescriptor";
import {DECODER_UNKNOWN_NAME} from "../../core/encoding/Decoding";
import {OtherSerialType} from "../../core/SerialKind";


export function getJsonElementName(serialDescriptor: SerialDescriptor, json: Json, index: number): string {
    return serialDescriptor.getElementName(index)
    // Don't delete:
    // const strategy = namingStrategy(json, serialDescriptor);
    // return strategy === null ? serialDescriptor.getElementName(index) : serializationNamesIndices(serialDescriptor, json, strategy)[index];
}

export function getJsonNameIndexOrThrow(serialDescriptor: SerialDescriptor, json: Json, name: string, suffix: string = ""): number {
    return getJsonNameIndex(serialDescriptor, json, name)
}


export function tryCoerceValue(
    json: Json, // Replace 'any' with the actual type of your Json object
    elementDescriptor: SerialDescriptor, // Replace with the actual type of SerialDescriptor
    peekNull: (consume: boolean) => boolean,
    peekString: () => string | null,
    onEnumCoercing: () => void = () => {
    }
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
        const enumIndex = getJsonNameIndex(elementDescriptor, json, enumValue)
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
    // Don't delete:
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