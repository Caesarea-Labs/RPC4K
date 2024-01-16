import {PluginGeneratedSerialDescriptor} from "./PluginGeneratedSerialDescriptor";
import {SerialDescriptor} from "../core/SerialDescriptor";
import {OtherSerialType, StructureKind} from "../core/SerialKind";
import {buildSerialDescriptor, getDescriptorElementNames} from "../descriptors/SerialDescriptors";

export class EnumDescriptor extends PluginGeneratedSerialDescriptor {
    private elementDescriptors: SerialDescriptor[];

    constructor(name: string, elementsCount: number) {
        super(name,null, elementsCount);
        this.kind = OtherSerialType.ENUM;
        this.elementDescriptors = Array.from({ length: elementsCount }, (_, i) =>
            buildSerialDescriptor(name + "." + this.getElementName(i), StructureKind.SINGLETON,[])
        );
    }

    getElementDescriptor(index: number): SerialDescriptor {
        return this.elementDescriptors[index]; // Simplified; consider adding bounds checking
    }


    toString(): string {
        // Assuming elementNames is a property that needs to be defined
        return `${this.serialName}(${getDescriptorElementNames(this).join(", ")})`;
    }
}