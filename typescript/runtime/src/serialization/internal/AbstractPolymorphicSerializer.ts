import {DeserializationStrategy, SerializationStrategy, TsSerializer} from "../TsSerializer";
import {SerialDescriptor} from "../core/SerialDescriptor";
import {CompositeDecoder, Decoder, DECODER_DECODE_DONE} from "../core/encoding/Decoding";
import {Encoder} from "../core/encoding/Encoder";
import {TsClass} from "../polyfills/TsClass";
import {SerializationException} from "../core/SerializationException";
import {RpcTypeDiscriminator} from "../../impl/RpcTypeDiscriminator";

/**
 * Base class for providing multiplatform polymorphic serialization.
 *
 * This class cannot be implemented by library users. To learn how to use it for your case,
 * please refer to documentation for polymorphic serialization for interfaces/abstract classes.
 *
 * By default, without special support from Encoder, polymorphic types are serialized as list with
 * two elements: class serial name (String) and the object itself.
 * Serial name equals to fully-qualified class name by default.
 */
export abstract class AbstractPolymorphicSerializer<T> implements TsSerializer<T> {
    abstract descriptor: SerialDescriptor;

    /**
     * Base class for all classes that this polymorphic serializer can serialize or deserialize.
     */
    public abstract baseClass: TsClass;

    /**
     * Extends the 'type' value to include the fully qualified rpc type name.
     * In the future we won't need to do this because package names may be omitted.
     */
    abstract resolveRpcType(shortName: string): string

    public serialize(encoder: Encoder, value: T) {
        const actualSerializer = this.findPolymorphicSerializer(encoder, value);
        const composite = encoder.beginStructure(this.descriptor)
        composite.encodeStringElement(this.descriptor, 0, actualSerializer.descriptor.serialName);
        composite.encodeSerializableElement(this.descriptor, 1, actualSerializer, value);
        composite.endStructure(this.descriptor)
    }

    public deserialize(decoder: Decoder): T {
        let klassName: string | null = null;
        let value: unknown = null;
        const composite = decoder.beginStructure(this.descriptor)

        if (composite.decodeSequentially()) {
            return this.decodeSequentially(composite);
        }

        while (true) {
            const index = composite.decodeElementIndex(this.descriptor);
            if (index === DECODER_DECODE_DONE) {
                break;
            } else if (index === 0) {
                klassName = composite.decodeStringElement(this.descriptor, index);
            } else if (index === 1) {
                if (klassName === null) {
                    throw new Error("Cannot read polymorphic value before its type token");
                }
                const serializer = this.findPolymorphicDeserializer(composite, klassName);
                value = composite.decodeSerializableElement(this.descriptor, index, serializer);
            } else {
                throw new Error(`Invalid index in polymorphic deserialization of ${klassName ?? "unknown class"}\nExpected 0, 1 or DECODE_DONE(-1), but found ${index}`);
            }
        }

        if (value === null) {
            throw new Error(`Polymorphic value has not been read for class ${klassName}`);
        }
        composite.endStructure(this.descriptor)

        return value as T;
    }

    private decodeSequentially(decoder: CompositeDecoder): T {
        const klassName = decoder.decodeStringElement(this.descriptor, 0);
        const serializer = this.findPolymorphicDeserializer(decoder, klassName);
        return decoder.decodeSerializableElement(this.descriptor, 1, serializer);
    }


    /**
     * Lookups an actual serializer for given klassName within the current base class.
     * May use context from the decoder.
     */
    public findPolymorphicSerializerOrNullForClassName(
        decoder: CompositeDecoder,
        klassName: string | null
    ): DeserializationStrategy<T> | null {  // Replace 'any' with the appropriate type for your deserialization strategy
        // Implement the logic to get the polymorphic serializer based on the class name and decoder context
        // This is a placeholder implementation
        return decoder.serializersModule?.getPolymorphicDeserialization(this.baseClass, klassName) ?? null;
    }

    /**
     * Lookups an actual serializer for given value within the current base class.
     * May use context from the encoder.
     */
    public findPolymorphicSerializerOrNullForValue(
        encoder: Encoder,
        value: T
    ): SerializationStrategy<T> | null {
        return encoder.serializersModule?.getPolymorphicSerialization(this.baseClass, value, this.getTsClass(value)) ?? null;
    }

    getTsClass(value: T): string {
        if (typeof value === "object" && value !== null && RpcTypeDiscriminator in value) {
            const discriminator = value[RpcTypeDiscriminator]
            if (typeof discriminator !== "string") throw new Error(`Non-string type discriminator: ${JSON.stringify(discriminator)}`)
            return this.resolveRpcType(discriminator)
        } else {
            throw new Error(`Missing type discriminator in polymorphic value: ${JSON.stringify(value)}`)
        }
    }

    findPolymorphicDeserializer(decoder: CompositeDecoder, klassName: string | null): DeserializationStrategy<T> {
        return this.findPolymorphicSerializerOrNullForClassName(decoder, klassName) ?? throwSubtypeNotRegistered(klassName, this.baseClass);
    }

    findPolymorphicSerializer(encoder: Encoder, value: T): SerializationStrategy<T> {
        return this.findPolymorphicSerializerOrNullForValue(encoder, value) ?? throwSubtypeNotRegistered(this.getTsClass(value), this.baseClass);
    }

}

function throwSubtypeNotRegistered(subClassName: string | null, baseClass: TsClass): never {
    const scope = `in the scope of '${baseClass}'`;
    throw new SerializationException(
        subClassName === null
            ? `Class discriminator was missing and no default polymorphic serializers were registered ${scope}`
            : `Class '${subClassName}' is not registered for polymorphic serialization ${scope}.\n` +
            `To be registered automatically, class '${subClassName}' has to be '@Serializable', and the base class '${baseClass}' has to be sealed and '@Serializable'.\n` +
            `Alternatively, register the serializer for '${subClassName}' explicitly in a corresponding SerializersModule.`
    );
}