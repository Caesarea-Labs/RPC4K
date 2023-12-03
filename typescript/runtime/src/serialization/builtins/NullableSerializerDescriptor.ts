import {SerialDescriptor} from "../core/SerialDescriptor";
import { SerialKind } from "../core/SerialKind";

export class NullableSerializerDescriptor implements SerialDescriptor {
    original: SerialDescriptor
    kind: SerialKind
    isNullable: boolean;
    isInline?: boolean | undefined;
    elementsCount: number;
    serialName: string;
    constructor(original: SerialDescriptor) {
        this.original = original
        this.kind = original.kind
        this.isNullable = true
        this.elementsCount = original.elementsCount
        this.serialName = original.serialName + "?"
    }


    getElementName(index: number): string {
        return this.original.getElementName(index)
    }
    getElementIndex(name: string): number {
        return this.original.getElementIndex(name)
    }
    getElementDescriptor(index: number): SerialDescriptor {
        return this.original.getElementDescriptor(index)
    }
    isElementOptional(index: number): boolean {
        return this.original.isElementOptional(index)
    }

    toString() {
        return `${this.original}?`
    }
}
