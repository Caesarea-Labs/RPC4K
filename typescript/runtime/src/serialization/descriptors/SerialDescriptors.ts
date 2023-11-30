/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
import {SerialDescriptor} from "../core/SerialDescriptor";
import {SerialKind, StructureKind} from "../core/SerialKind";
import {DECODER_UNKNOWN_NAME} from "../core/encoding/Decoding";

export function buildClassSerialDescriptor(
    serialName: string,
    typeParameters: SerialDescriptor[] = [],
    builderAction: (builder: ClassSerialDescriptorBuilder) => void = () => {}
): SerialDescriptor {
    if (!serialName.trim()) {
        throw new Error("Blank serial names are prohibited");
    }

    const sdBuilder = new ClassSerialDescriptorBuilder(serialName);
    builderAction(sdBuilder);

    return new SerialDescriptorImpl(
        serialName,
        StructureKind.OBJECT,
        sdBuilder.elementNames.length,
        typeParameters,
        sdBuilder
    );
}

export class SerialDescriptorImpl implements SerialDescriptor {
    private serialNames: Set<string>;
    private elementNames: string[];
    private elementDescriptors: SerialDescriptor[];
    private elementOptionality: boolean[];
    private name2Index: Map<string, number>;
    private typeParametersDescriptors: SerialDescriptor[];
    isNullable = false

    constructor(
        public serialName: string,
        public kind: SerialKind,
        public elementsCount: number,
        typeParameters: SerialDescriptor[],
        builder: ClassSerialDescriptorBuilder
    ) {
        this.serialNames = new Set(builder.elementNames);

        this.elementNames = [...builder.elementNames];
        this.elementDescriptors = builder.elementDescriptors; // Adjust based on actual implementation
        this.elementOptionality = builder.elementOptionality; // Adjust based on actual implementation
        this.name2Index = new Map(this.elementNames.map((name, index) => [name, index]));
        this.typeParametersDescriptors = typeParameters;
    }

    getElementName(index: number): string {
        return this.elementNames[index]; // Add bounds checking as needed
    }

    getElementIndex(name: string): number {
        return this.name2Index.get(name) ?? DECODER_UNKNOWN_NAME;
    }


    getElementDescriptor(index: number): SerialDescriptor {
        return this.elementDescriptors[index]; // Add bounds checking as needed
    }

    isElementOptional(index: number): boolean {
        return this.elementOptionality[index]; // Add bounds checking as needed
    }


    toString(): string {
        return `${this.serialName}(${this.elementNames.map((name, index) => `${name}: ${this.getElementDescriptor(index).serialName}`).join(", ")})`;
    }
}

export class ClassSerialDescriptorBuilder {
    public serialName: string;
    public isNullable: boolean = false;

     elementNames: string[] = [];
     uniqueNames: Set<string> = new Set<string>();
     elementDescriptors: SerialDescriptor[] = [];
     elementOptionality: boolean[] = [];

    constructor(serialName: string) {
        this.serialName = serialName;
    }

    public element(
        elementName: string,
        descriptor: SerialDescriptor,
        isOptional: boolean = false
    ): void {
        if (this.uniqueNames.has(elementName)) {
            throw new Error(`Element with name '${elementName}' is already registered in ${this.serialName}`);
        }
        this.uniqueNames.add(elementName);
        this.elementNames.push(elementName);
        this.elementDescriptors.push(descriptor);
        this.elementOptionality.push(isOptional);
    }
}