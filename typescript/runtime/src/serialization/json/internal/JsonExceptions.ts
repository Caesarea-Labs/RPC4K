/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
import {SerialDescriptor} from "../../core/SerialDescriptor";
import {allowStructuredMapKeysHint, specialFlowingValuesHint} from "./AbstractJsonLexer";

export function InvalidKeyKindException(keyDescriptor: SerialDescriptor): JsonEncodingException {
    const errorMessage = `Value of type '${keyDescriptor.serialName}' can't be used in JSON as a key in the map. ` +
        `It should have either primitive or enum kind, but its kind is '${keyDescriptor.kind}'.\n` +
        allowStructuredMapKeysHint; // Assuming allowStructuredMapKeysHint is a predefined constant or variable

    return new JsonEncodingException(errorMessage);
}

export function InvalidFloatingPointEncoded(value: number, output: string):  JsonEncodingException {
    return new JsonEncodingException(
        `Unexpected special floating-point value ${value}. By default, ` +
        "non-finite floating point values are prohibited because they do not conform JSON specification. " +
        `${specialFlowingValuesHint}\n` +
        `Current output: ${minify(output)}`
    )
}


export class JsonException extends Error {
    constructor(message: string) {
        super(message);
    }
}

export class JsonEncodingException extends JsonException {
    constructor(message: string) {
        super(message);
    }
}
/**
 * Thrown when JSON processing fails to parse a given JSON string or deserialize it to a target class.
 */
export class JsonDecodingException extends JsonException {
    constructor(message: string) {
        super(message);
    }
}

export function createJsonDecodingException(offset: number, message: string): JsonDecodingException {
    const errorMessage = offset >= 0 ? `Unexpected JSON token at offset ${offset}: ${message}` : message;
    return new JsonDecodingException(errorMessage);
}


export function createJsonDecodingExceptionWithInput(offset: number, message: string, input: string): JsonDecodingException {
    const errorMessage = `${message}\nJSON input: ${minify(input, offset)}`; // Assuming minifyInput is a function that minifies the input
    return new JsonDecodingException(errorMessage);
}

function minify(input: string, offset: number = -1): string {
    if (input.length < 200) return input;
    if (offset === -1) {
        const start = input.length - 60;
        if (start <= 0) return input;
        return "....." + input.substring(start);
    }

    const start = Math.max(0, offset - 30);
    const end = Math.min(input.length, offset + 30);
    const prefix = start <= 0 ? "" : ".....";
    const suffix = end >= input.length ? "" : ".....";
    return prefix + input.substring(start, end) + suffix;
}