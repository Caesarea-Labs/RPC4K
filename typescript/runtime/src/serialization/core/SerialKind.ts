/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
export type SerialKind = OtherSerialType | PrimitiveKind | StructureKind | PolymorphicKind

export enum OtherSerialType {
    ENUM = "ENUM",
    CONTEXTUAL = "CONTEXTUAL",
}

export enum PrimitiveKind {
    BOOLEAN = "BOOLEAN",
    NUMBER = "NUMBER",
    STRING = "STRING"
}

export function isPrimitiveKind(kind: SerialKind): kind is PrimitiveKind {
    return kind === PrimitiveKind.BOOLEAN || kind == PrimitiveKind.NUMBER || kind == PrimitiveKind.STRING
}

export enum StructureKind {
    LIST = "LIST",
    OBJECT = "OBJECT",
    MAP = "MAP"
}

export enum PolymorphicKind {
    OPEN = "OPEN"
}
