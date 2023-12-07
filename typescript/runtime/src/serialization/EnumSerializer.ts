import {TsSerializer} from "./TsSerializer";
import {SerialDescriptor} from "./core/SerialDescriptor";
import {EnumDescriptor} from "./internal/EnumDescriptor";
import {Encoder} from "./core/encoding/Encoder";
import {Decoder} from "./core/encoding/Decoding";

export class EnumSerializer<T extends string> implements TsSerializer<T> {
    private overriddenDescriptor: SerialDescriptor | null = null;
    private values: T[]

    constructor(private serialName: string, values: T[], descriptor?: SerialDescriptor) {
        this.values = values
        if (descriptor) {
            this.overriddenDescriptor = descriptor;
        }
    }

    get descriptor(): SerialDescriptor {
        if (this.overriddenDescriptor) {
            return this.overriddenDescriptor;
        }

        return this.createUnmarkedDescriptor(this.serialName);
    }

    private createUnmarkedDescriptor(serialName: string): SerialDescriptor {
        const d = new EnumDescriptor(serialName, this.values.length)
        for(const value of this.values) {
            d.addElement(value)
        }
        return d
    }

    serialize(encoder: Encoder, value: T): void {
        const index = this.values.indexOf(value);
        if (index === -1) {
            throw new Error(`${value} is not a valid enum ${this.descriptor.serialName}, must be one of ${this.values.toString()}`);
        }
        encoder.encodeEnum(this.descriptor, index);
    }

    deserialize(decoder: Decoder): T {
        const index = decoder.decodeEnum(this.descriptor);
        if (index < 0 || index >= this.values.length) {
            throw new Error(`${index} is not among valid ${this.descriptor.serialName} enum values, values size is ${this.values.length}`);
        }
        return this.values[index];
    }

    toString(): string {
        return `EnumSerializer<${this.descriptor.serialName}>`;
    }
}