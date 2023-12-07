import {SerialDescriptor} from "../core/SerialDescriptor";
import {PrimitiveKind} from "../core/SerialKind";

export class PrimitiveSerialDescriptor extends SerialDescriptor {
    serialName: string;
    kind: PrimitiveKind;

    constructor(serialName: string, kind: PrimitiveKind) {
        super()
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
}