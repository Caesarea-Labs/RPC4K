/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
import {SerialKind} from "./SerialKind";

/**
 * Serial descriptor interface declaration.
 */
export abstract class SerialDescriptor {
    abstract kind: SerialKind;

    isNullable: boolean = false

    isInline: boolean = false

    abstract elementsCount: number;

    abstract serialName: string

    abstract getElementName(index: number): string;

    abstract getElementIndex(name: string): number;

    abstract getElementDescriptor(index: number): SerialDescriptor;

    abstract isElementOptional(index: number): boolean;
}


export function isSerialDescriptor(obj: unknown): obj is SerialDescriptor {
    return obj !== null && typeof obj === "object" && "kind" in obj && "elementsCount" in obj
}