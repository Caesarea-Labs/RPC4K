import {AbstractPolymorphicSerializer} from "./internal/AbstractPolymorphicSerializer";
import {DeserializationStrategy, SerializationStrategy, TsSerializer} from "./TsSerializer";
import {TsClass} from "./polyfills/TsClass";
import {SerialDescriptor} from "./core/SerialDescriptor";
import {buildSerialDescriptor} from "./descriptors/SerialDescriptors";
import {OtherSerialType, PolymorphicKind} from "./core/SerialKind";
import {StringSerializer} from "./BuiltinSerializers";
import {CompositeDecoder} from "./core/encoding/Decoding";
import {Encoder} from "./core/encoding/Encoder";


export class UnionSerializer<T> extends AbstractPolymorphicSerializer<T> {
    private class2Serializer: Map<TsClass, TsSerializer<T>>
    private serialName2Serializer: Map<string, TsSerializer<T>> = new Map();
    private _descriptor: SerialDescriptor | undefined;

    resolveRpcType(shortName: string): string {
        const resolved = this.fullTypeNames[shortName]
        if (resolved === undefined) {
            throw new Error(`Unknown type name: ${shortName}, Available names: ${Object.keys(this.fullTypeNames)}`)
        }
        return resolved
    }

    constructor(
        private serialName: string,
        public baseClass: TsClass,
        subclasses: TsClass[],
        subclassSerializers: TsSerializer<T>[],
        private fullTypeNames: Record<string, string>
    ) {
        super();
        if (subclasses.length !== subclassSerializers.length) {
            throw new Error(`All subclasses of sealed class ${this.baseClass} should be marked @Serializable`);
        }

        this.class2Serializer = new Map(subclasses.map((subclass, index) => [subclass, subclassSerializers[index]]));

        this.class2Serializer.forEach((serializer, kClass) => {
            const serialName = serializer.descriptor.serialName;
            if (this.serialName2Serializer.has(serialName)) {
                throw new Error(`Multiple sealed subclasses of '${this.baseClass}' have the same serial name '${serialName}'`);
            }
            this.serialName2Serializer.set(serialName, serializer);
        });
    }

    get descriptor(): SerialDescriptor {
        if (this._descriptor === undefined) {
            this._descriptor = this.buildDescriptor();
        }
        return this._descriptor;
    }

    private buildDescriptor(): SerialDescriptor {
        // Assuming buildSerialDescriptor and String.serializer().descriptor are implemented
        const elementDescriptor = buildSerialDescriptor(`kotlinx.serialization.Sealed<${this.baseClass}>`, OtherSerialType.CONTEXTUAL, [], (builder) => {
            this.serialName2Serializer.forEach((serializer, name) => {
                builder.element(name, serializer.descriptor)
            });
        });

        return buildSerialDescriptor(this.serialName, PolymorphicKind.SEALED, [], (builder) => {
            builder.element("type", StringSerializer.descriptor)
            builder.element("value", elementDescriptor)
        });
    }

    override findPolymorphicSerializerOrNullForClassName(decoder: CompositeDecoder, klassName: string | null): DeserializationStrategy<T> | null {
        return this.serialName2Serializer.get(klassName ?? '') ?? super.findPolymorphicSerializerOrNullForClassName(decoder, klassName);
    }

    override findPolymorphicSerializerOrNullForValue(encoder: Encoder, value: T): SerializationStrategy<T> | null {
        return this.class2Serializer.get(this.getTsClass(value)) ?? super.findPolymorphicSerializerOrNullForValue(encoder, value);
    }

}