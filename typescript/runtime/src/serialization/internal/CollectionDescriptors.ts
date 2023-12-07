import {SerialDescriptor} from "../core/SerialDescriptor";
import {SerialKind, StructureKind} from "../core/SerialKind";

abstract class ListLikeDescriptor extends SerialDescriptor {
    private _elementDescriptor: SerialDescriptor;

    constructor(elementDescriptor: SerialDescriptor) {
        super()
        this._elementDescriptor = elementDescriptor;
    }

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

abstract class MapLikeDescriptor extends SerialDescriptor {
    serialName: string;
    keyDescriptor: SerialDescriptor;
    valueDescriptor: SerialDescriptor;

    protected constructor(serialName: string, keyDescriptor: SerialDescriptor, valueDescriptor: SerialDescriptor) {
        super()
        this.serialName = serialName;
        this.keyDescriptor = keyDescriptor;
        this.valueDescriptor = valueDescriptor;
    }

    get kind(): SerialKind {
        return StructureKind.MAP;
    }

    get elementsCount(): number {
        return 2;
    }

    getElementName(index: number): string {
        return index.toString();
    }

    getElementIndex(name: string): number {
        const index = parseInt(name);
        if (isNaN(index)) {
            throw new Error(`${name} is not a valid map index`);
        }
        return index;
    }

    isElementOptional(index: number): boolean {
        if (index < 0) {
            throw new Error(`Illegal index ${index}, ${this.serialName} expects only non-negative indices`);
        }
        return false;
    }

    getElementAnnotations(index: number): any[] {
        if (index < 0) {
            throw new Error(`Illegal index ${index}, ${this.serialName} expects only non-negative indices`);
        }
        return [];
    }

    getElementDescriptor(index: number): SerialDescriptor {
        if (index < 0) {
            throw new Error(`Illegal index ${index}, ${this.serialName} expects only non-negative indices`);
        }
        switch (index % 2) {
            case 0:
                return this.keyDescriptor;
            case 1:
                return this.valueDescriptor;
            default:
                throw new Error("Unreached");
        }
    }

    toString(): string {
        return `${this.serialName}(${this.keyDescriptor}, ${this.valueDescriptor})`;
    }
}

export class RecordDesc extends MapLikeDescriptor {
    constructor(keyDesc: SerialDescriptor, valueDesc: SerialDescriptor) {
        super("record", keyDesc, valueDesc);
    }
}

