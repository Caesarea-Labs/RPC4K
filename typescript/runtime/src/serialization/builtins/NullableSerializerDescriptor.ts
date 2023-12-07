import {SerialDescriptor} from "../core/SerialDescriptor";
import { SerialKind } from "../core/SerialKind";

export class NullableSerializerDescriptor extends SerialDescriptor {
    original: SerialDescriptor
    kind: SerialKind
    isNullable: boolean = true
    elementsCount: number;
    serialName: string;
    constructor(original: SerialDescriptor) {
        super()
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
