/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
import {PrimitiveKind} from "../core/SerialKind";
import {SerialDescriptor} from "../core/SerialDescriptor";
import {TsSerializer} from "../core/TsSerializer";
import {Encoder} from "../core/encoding/Encoder";
import {Decoder} from "../core/encoding/Decoding";

export class PrimitiveSerialDescriptor implements SerialDescriptor{
    serialName: string;
    kind: PrimitiveKind;

    constructor(serialName: string, kind: PrimitiveKind) {
        this.serialName = serialName;
        this.kind = kind;
    }

    get elementsCount(): number {
        return 0;
    }

    getElementName(index: number): string {
        throw new Error("Primitive descriptor does not have elements");
    }

    getElementIndex(name: string): number {
        throw new Error("Primitive descriptor does not have elements");
    }

    isElementOptional(index: number): boolean {
        throw new Error("Primitive descriptor does not have elements");
    }

    getElementDescriptor(index: number): SerialDescriptor {
        throw new Error("Primitive descriptor does not have elements");
    }

    toString(): string {
        return `PrimitiveDescriptor(${this.serialName})`;
    }

    isNullable: boolean = false
}

export const StringSerializer: TsSerializer<string> = {
    descriptor: new PrimitiveSerialDescriptor("javascript.string", PrimitiveKind.STRING),
    serialize(encoder: Encoder, value: string) {
        encoder.encodeString(value)
    },
    deserialize(decoder: Decoder): string {
        return decoder.decodeString()
    }
}

export const BooleanSerializer: TsSerializer<boolean> = {
    descriptor: new PrimitiveSerialDescriptor("javascript.boolean", PrimitiveKind.BOOLEAN),
    serialize(encoder: Encoder, value: boolean): void {
        encoder.encodeBoolean(value);
    },
    deserialize(decoder: Decoder): boolean {
        return decoder.decodeBoolean();
    }
};
export const NumberSerializer: TsSerializer<number> = {
    descriptor: new PrimitiveSerialDescriptor("javascript.number", PrimitiveKind.NUMBER),
    serialize(encoder: Encoder, value: number): void {
        encoder.encodeNumber(value);
    },
    deserialize(decoder: Decoder): number {
        return decoder.decodeNumber();
    }
};

