import {SerialDescriptor} from "../core/SerialDescriptor";
import {TsSerializer} from "../TsSerializer";
import {Decoder, DECODER_UNKNOWN_NAME} from "../core/encoding/Decoding";
import {SerialKind, StructureKind} from "../core/SerialKind";
import {Encoder} from "../core/encoding/Encoder";

export class PluginGeneratedSerialDescriptor implements SerialDescriptor {
    readonly serialName: string;
    private readonly generatedSerializer: GeneratedSerializer<unknown> | null;
    readonly elementsCount: number;
    kind: SerialKind; // Adjust this as needed
    isNullable = false
    private added: number = -1;
    private readonly names: string[];
    private readonly elementsOptionality: boolean[];
    private indices = new Map<string, number>();
    private readonly childSerializers: TsSerializer<any>[];

    constructor(serialName: string, generatedSerializer: GeneratedSerializer<any> | null = null, elementsCount: number) {
        this.serialName = serialName;
        this.generatedSerializer = generatedSerializer;
        this.elementsCount = elementsCount;

        this.kind = StructureKind.OBJECT
        this.names = Array(elementsCount).fill("[UNINITIALIZED]");
        this.elementsOptionality = Array(elementsCount).fill(false);
        this.childSerializers = this.generatedSerializer ? this.generatedSerializer.childSerializers() : [];
    }

    public addElement(name: string, isOptional: boolean = false): void {
        this.names[++this.added] = name;
        this.elementsOptionality[this.added] = isOptional;
        if (this.added === this.elementsCount - 1) {
            this.indices = this.buildIndices();
        }
    }


    public getElementDescriptor(index: number): SerialDescriptor {
        return this.childSerializers[index].descriptor; // Assuming getChecked logic is handled in array access
    }

    // Convert 'isElementOptional' function
    public isElementOptional(index: number): boolean {
        return this.elementsOptionality[index]; // Assuming getChecked logic is handled in array access
    }

    // Convert 'getElementName' function
    public getElementName(index: number): string {
        return this.names[index]; // Assuming getChecked logic is handled in array access
    }

    // Convert 'getElementIndex' function
    public getElementIndex(name: string): number {
        return this.indices.get(name) ?? DECODER_UNKNOWN_NAME;
    }

    // Convert 'buildIndices' function
    private buildIndices(): Map<string, number> {
        const indices = new Map<string, number>();
        this.names.forEach((name, i) => {
            indices.set(name, i);
        });
        return indices;
    }


    // Convert 'toString' function
    public toString(): string {
        return `${this.serialName}(${this.names.map((name, i) => `${name}: ${this.getElementDescriptor(i).serialName}`).join(", ")})`;
    }
}

export abstract class GeneratedSerializer<T> implements TsSerializer<T> {
    public abstract childSerializers(): TsSerializer<any>[]

    public typeParametersSerializers(): TsSerializer<any>[] {
        return []
    }

    abstract descriptor: SerialDescriptor;

    abstract deserialize(decoder: Decoder): T

    abstract serialize(encoder: Encoder, value: T): void
}
