import {TsSerializer} from "./TsSerializer";
import {SerialDescriptor} from "./core/SerialDescriptor";
import {Encoder} from "./core/encoding/Encoder";
import {Decoder, DECODER_DECODE_DONE} from "./core/encoding/Decoding";
import {GeneratedSerializer, PluginGeneratedSerialDescriptor} from "./internal/PluginGeneratedSerializer";
import {mapRecordValues, recordForEach, recordToArray} from "ts-minimum";

/**
 * Serializers may be lazy so that recursively defined serializers may be used
 */
export type SerializerMap<T> = Record<keyof T, (() => TsSerializer<unknown>) | TsSerializer<unknown>>

export class GeneratedSerializerImpl<T> extends GeneratedSerializer<T> {
    private readonly serializers: SerializerMap<T>

    /**
     * Resolve lazy serializers, so it should only be used when the serializer is actually used
     * @private
     */
    private getSerializerMap(): Record<keyof T, TsSerializer<unknown>> {
       return mapRecordValues(this.serializers,
            (_, serializer) => typeof serializer === "function" ? serializer() : serializer
        ) 
    }

    private readonly typeParamSerializers: TsSerializer<unknown>[]

    typeParametersSerializers(): TsSerializer<unknown>[] {
        return this.typeParamSerializers
    }

    private readonly elementIndices: (keyof T)[]
    private readonly construct: (params: unknown) => T

    public childSerializers(): TsSerializer<unknown>[] {
        return Object.values(this.serializers)
    }
    // eslint-disable-next-line
    constructor(name: string, serializers: SerializerMap<T>, typeParameterSerializers: TsSerializer<unknown>[], constructor: (params: any) => T) {
        super()
        this.typeParamSerializers = typeParameterSerializers
        this.serializers = serializers
        const descriptor = new PluginGeneratedSerialDescriptor(name, this, Object.values(serializers).length)
        for (const serializerName in serializers) {
            descriptor.addElement(serializerName, false) //TODO: optionals
        }
        this.descriptor = descriptor

        // buildClassSerialDescriptor(name,
        //     typeParameterSerializers.map(serializer => serializer.descriptor),
        //     (builder) => {
        //         recordForEach(serializers(), (elementName, serializer) => {
        //             builder.element(elementName, serializer.descriptor)
        //         })
        //     })
        this.elementIndices = recordToArray(serializers, (k) => k)
        this.construct = constructor
    }


    descriptor: SerialDescriptor;

    serialize(encoder: Encoder, value: T): void {
        const compositeEncoder = encoder.beginStructure(this.descriptor)
        let i = 0;
        recordForEach(this.getSerializerMap(), (elementName, serializer) => {
            compositeEncoder.encodeSerializableElement(this.descriptor, i, serializer, value[elementName])
            i++
        })
        compositeEncoder.endStructure(this.descriptor)
    }

    deserialize(decoder: Decoder): T {
        const compositeDecoder = decoder.beginStructure(this.descriptor);
        const values: Partial<T> = {}
        const serializers = this.getSerializerMap()

        while (true) {
            const index = compositeDecoder.decodeElementIndex(this.descriptor);
            if (index === DECODER_DECODE_DONE) {
                break;
            }

            if (index >= this.elementIndices.length) {
                throw new Error(`Unexpected index: ${index}`);
            } else {
                const key = this.elementIndices[index]
                values[key] = compositeDecoder.decodeSerializableElement(this.descriptor, index, serializers[key]) as T[keyof T]
            }
        }

        compositeDecoder.endStructure(this.descriptor);

        for (const key in this.serializers) {
            if (!(key in values)) {
                throw new Error(`Missing field: ${key}`);
            }
        }
        return this.construct(values)
    }

}