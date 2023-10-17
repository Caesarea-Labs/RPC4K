import {SerialKind} from "./SerialKind";

/**
 * Serial descriptor interface declaration.
 */
export interface SerialDescriptor {
    kind: SerialKind;

    isNullable?: boolean;

    isInline?: boolean;

    elementsCount: number;

    getElementName(index: number): string;

    getElementIndex(name: string): number;

    getElementDescriptor(index: number): SerialDescriptor;

    isElementOptional(index: number): boolean;
}
