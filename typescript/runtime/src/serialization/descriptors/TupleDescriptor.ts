import {SerialDescriptor} from "../core/SerialDescriptor";
import {StructureKind} from "../core/SerialKind";

export class TupleDescriptor extends SerialDescriptor {
    private readonly elementDescriptors: SerialDescriptor[];
    public readonly kind = StructureKind.LIST;
    public readonly elementsCount: number;
    public readonly serialName: string = "Tuple";

    constructor(elementDescriptors: SerialDescriptor[]) {
        super()
        this.elementDescriptors = elementDescriptors;
        this.elementsCount = elementDescriptors.length;
    }

    getElementName(index: number): string {
        return index.toString();
    }

    getElementIndex(name: string): number {
        const index = parseInt(name);
        if (isNaN(index)) {
            throw new Error(`${name} is not a valid list index`);
        }
        return index;
    }

    isElementOptional(index: number): boolean {
        this.validateIndex(index);
        return false;
    }

    getElementDescriptor(index: number): SerialDescriptor {
        this.validateIndex(index);
        return this.elementDescriptors[index];
    }

    toString(): string {
        return `${this.serialName}(${this.elementDescriptors.join(", ")})`;
    }

    private validateIndex(index: number): void {
        if (index < 0 || index >= this.elementsCount) {
            throw new Error(`Illegal index ${index}, ${this.serialName} expects only indices in the range 0 to ${this.elementsCount - 1}`);
        }
    }
}