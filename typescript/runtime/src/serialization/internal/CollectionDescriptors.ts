import {SerialDescriptor} from "../core/SerialDescriptor";
import {SerialKind, StructureKind} from "../core/SerialKind";

abstract class ListLikeDescriptor implements SerialDescriptor {
    private _elementDescriptor: SerialDescriptor;

    constructor(elementDescriptor: SerialDescriptor) {
        this._elementDescriptor = elementDescriptor;
    }

    isNullable: boolean = false
    abstract serialName: string;

    get kind(): SerialKind {
        return StructureKind.LIST;
    }

    get elementsCount(): number {
        return 1;
    }

    getElementName(index: number): string {
        return index.toString();
    }

    getElementIndex(name: string): number {
        const parsed = parseInt(name);
        if (isNaN(parsed)) {
            throw new Error(`${name} is not a valid list index`);
        }
        return parsed;
    }

    isElementOptional(index: number): boolean {
        if (index < 0) {
            throw new Error(`Illegal index ${index}, serialName expects only non-negative indices`);
        }
        return false;
    }


    getElementDescriptor(index: number): SerialDescriptor {
        if (index < 0) {
            throw new Error(`Illegal index ${index}, serialName expects only non-negative indices`);
        }
        return this._elementDescriptor;
    }

    // equals(other: any): boolean {
    //     if (this === other) return true;
    //     if (!(other instanceof ListLikeDescriptor)) return false;
    //     return this._elementDescriptor === other._elementDescriptor && this.serialName === other.serialName;
    // }
    //
    // hashCode(): number {
    //     return this._elementDescriptor.hashCode() * 31 + this.serialName.hashCode();
    // }

    toString(): string {
        return `${this.serialName}(${this._elementDescriptor})`;
    }

    // You would need to implement `serialName` and `hashCode` methods for `SerialDescriptor`
    // TypeScript doesn't have a built-in hashCode method, and `serialName` would be specific to your implementation
}

export class ArrayDesc extends ListLikeDescriptor {
    constructor(elementDesc: SerialDescriptor) {
        super(elementDesc)
    }
    serialName = ARRAY_NAME
}

export const ARRAY_NAME = "javascript.Array"
