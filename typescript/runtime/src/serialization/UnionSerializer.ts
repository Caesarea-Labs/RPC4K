import {AbstractPolymorphicSerializer} from "./internal/AbstractPolymorphicSerializer";
import {DeserializationStrategy, SerializationStrategy, TsSerializer} from "./TsSerializer";
import {getTsClass, TsClass} from "./polyfills/TsClass";
import {SerialDescriptor} from "./core/SerialDescriptor";
import {buildSerialDescriptor} from "./descriptors/SerialDescriptors";
import {OtherSerialType, PolymorphicKind} from "./core/SerialKind";
import {StringSerializer} from "./BuiltinSerializers";
import {CompositeDecoder} from "./core/encoding/Decoding";
import {Encoder} from "./core/encoding/Encoder";

export class UnionSerializer<T> extends AbstractPolymorphicSerializer<T> {
    private class2Serializer: Map<TsClass<T>, TsSerializer<T>>;
    private serialName2Serializer: Map<string, TsSerializer<T>>;
    private _descriptor: SerialDescriptor | undefined;

    constructor(
        private serialName: string,
        public baseClass: TsClass<T>,
        private subclasses: TsClass<unknown>[],
        private subclassSerializers: TsSerializer<T>[]
    ) {
        super();
        if (subclasses.length !== subclassSerializers.length) {
            throw new Error(`All subclasses of sealed class ${this.baseClass} should be marked @Serializable`);
        }

        this.class2Serializer = new Map(subclasses.map((subclass, index) => [subclass, subclassSerializers[index]]));

        // Constructing serialName2Serializer
        this.serialName2Serializer = new Map();
        this.class2Serializer.forEach((serializer, kClass) => {
            const serialName = serializer.descriptor.serialName;
            if (this.serialName2Serializer.has(serialName)) {
                throw new Error(`Multiple sealed subclasses of '${this.baseClass}' have the same serial name '${serialName}'`);
            }
            this.serialName2Serializer.set(serialName, serializer);
        });
    }

    get descriptor(): SerialDescriptor {
        if (!this._descriptor) {
            this._descriptor = this.buildDescriptor();
        }
        return this._descriptor;
    }

    private buildDescriptor(): SerialDescriptor {
        // Assuming buildSerialDescriptor and String.serializer().descriptor are implemented
        const elementDescriptor = buildSerialDescriptor(`kotlinx.serialization.Sealed<${this.baseClass}>`, OtherSerialType.CONTEXTUAL, [],(builder) => {
            this.serialName2Serializer.forEach((serializer, name) => {
                builder.element(name, serializer.descriptor)
            });
        });

        return buildSerialDescriptor(this.serialName, PolymorphicKind.SEALED, [],(builder) => {
            builder.element("type", StringSerializer.descriptor)
            builder.element("value", elementDescriptor)
        });
    }

    override findPolymorphicSerializerOrNullForClassName(decoder: CompositeDecoder, klassName: string | null): DeserializationStrategy<T> | null {
        return this.serialName2Serializer.get(klassName ?? '') ?? super.findPolymorphicSerializerOrNullForClassName(decoder, klassName);
    }

    override findPolymorphicSerializerOrNullForValue(encoder: Encoder, value: T): SerializationStrategy<T> | null {
        //TODO: this is probably gonna fail because of getTsClass implementation
        return this.class2Serializer.get(getTsClass(value)) ?? super.findPolymorphicSerializerOrNullForValue(encoder, value);
    }

}