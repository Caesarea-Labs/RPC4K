/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
import {SerialKind} from "./SerialKind";

/**
 * Serial descriptor interface declaration.
 */
export interface SerialDescriptor {
    kind: SerialKind;

    isNullable: boolean;

    isInline?: boolean;

    elementsCount: number;

    serialName: string

    getElementName(index: number): string;

    getElementIndex(name: string): number;

    getElementDescriptor(index: number): SerialDescriptor;

    isElementOptional(index: number): boolean;
}


export function isSerialDescriptor(obj: object): obj is SerialDescriptor {
    return obj !== null && "kind" in obj && "elementsCount" in obj
}