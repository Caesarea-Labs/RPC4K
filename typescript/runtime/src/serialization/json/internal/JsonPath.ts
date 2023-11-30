/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
import {isSerialDescriptor, SerialDescriptor} from "../../core/SerialDescriptor";
import {StructureKind} from "../../core/SerialKind";

/**
 * Internal representation of the current JSON path.
 * It is stored as the array of serial descriptors (for regular classes)
 * and any type in case of Map keys.
 *
 * Example of the state when decoding the list:
 * class Foo {
 *   constructor(public a: number, public l: string[]) {}
 * }
 *
 * // {"l": ["a", "b", "c"] }
 *
 * Current path when decoding array elements:
 * Foo.descriptor, List(String).descriptor
 * 1 (index of the 'l'), 2 (index of currently being decoded "c")
 */
export class JsonPath {
    private static Tombstone = Symbol("Tombstone");

    private currentObjectPath: (SerialDescriptor | any | typeof JsonPath.Tombstone)[] = new Array(8).fill(null);
    private indices: number[] = new Array(8).fill(-1);
    private currentDepth: number = -1;

    pushDescriptor(sd: SerialDescriptor): void {
        let depth = ++this.currentDepth;
        if (depth === this.currentObjectPath.length) {
            this.resize();
        }
        this.currentObjectPath[depth] = sd;
    }

    updateDescriptorIndex(index: number): void {
        this.indices[this.currentDepth] = index;
    }

    updateCurrentMapKey(key: any): void {
        if (this.indices[this.currentDepth] !== -2 && ++this.currentDepth === this.currentObjectPath.length) {
            this.resize();
        }
        this.currentObjectPath[this.currentDepth] = key;
        this.indices[this.currentDepth] = -2;
    }

    resetCurrentMapKey(): void {
        if (this.indices[this.currentDepth] === -2) {
            this.currentObjectPath[this.currentDepth] = JsonPath.Tombstone;
        }
    }

    popDescriptor(): void {
        let depth = this.currentDepth;
        if (this.indices[depth] === -2) {
            this.indices[depth] = -1;
            this.currentDepth--;
        }
        if (this.currentDepth !== -1) {
            this.currentDepth--;
        }
    }

    getPath(): string {
        let pathString = "$";

        for (let i = 0; i <= this.currentDepth; i++) {
            const element = this.currentObjectPath[i];

            if (isSerialDescriptor(element)) { // Assuming SerialDescriptor is defined
                if (element.kind === StructureKind.LIST) { // Assuming StructureKind is defined
                    if (this.indices[i] !== -1) {
                        pathString += `[${this.indices[i]}]`;
                    }
                } else {
                    const idx = this.indices[i];
                    if (idx >= 0) {
                        pathString += `.${element.getElementName(idx)}`; // Assuming getElementName method is defined
                    }
                }
            } else if (element !== JsonPath.Tombstone) { // Assuming Tombstone is defined
                pathString += `['${element}']`;
            }
        }

        return pathString;
    }

    private resize(): void {
        let newSize = this.currentDepth * 2;
        this.currentObjectPath = [...this.currentObjectPath, ...new Array(newSize - this.currentObjectPath.length).fill(null)];
        this.indices = [...this.indices, ...new Array(newSize - this.indices.length).fill(-1)];
    }

    toString(): string {
        return this.getPath();
    }
}
