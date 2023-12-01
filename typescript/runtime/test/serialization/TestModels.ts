import {TsSerializer} from "../../src/serialization/core/TsSerializer";
import {SerialDescriptor} from "../../src/serialization/core/SerialDescriptor";
import {buildClassSerialDescriptor} from "../../src/serialization/descriptors/SerialDescriptors";
import {BooleanSerializer, NumberSerializer, StringSerializer} from "../../src/serialization/builtins/BuiltinSerializers";
import {Encoder} from "../../src/serialization/core/encoding/Encoder";
import {ArraySerializer} from "../../src/serialization/internal/CollectionSerializers";
import {Decoder, DECODER_DECODE_DONE} from "../../src/serialization/core/encoding/Decoding";

export class AnotherModelHolder<T> {
    readonly t: GenericThing<T, string>
    constructor({t}:{t: GenericThing<T, string>}) {
        this.t = t;
    }
}

export class AnotherModelHolderSerializer<T> implements TsSerializer<AnotherModelHolder<T>> {
    private readonly tSerializer: TsSerializer<GenericThing<T, string>>
    constructor(serializer1: TsSerializer<T>) {
        this.tSerializer = new GenericThingSerializer(serializer1, StringSerializer)
        this.descriptor = buildClassSerialDescriptor("Foo", [serializer1.descriptor], (builder) => {
            builder.element("t", this.tSerializer.descriptor)
        })
    }


    descriptor: SerialDescriptor;
    serialize(encoder: Encoder, value: AnotherModelHolder<T>): void {
        const compositeEncoder = encoder.beginStructure(this.descriptor)
        compositeEncoder.encodeSerializableElement(this.descriptor, 0, this.tSerializer, value.t)
        compositeEncoder.endStructure(this.descriptor)
    }
    deserialize(decoder: Decoder): AnotherModelHolder<T> {
        const compositeDecoder = decoder.beginStructure(this.descriptor);
        let t: GenericThing<T, string> | null = null;

        while (true) {
            const index = compositeDecoder.decodeElementIndex(this.descriptor);
            if (index === DECODER_DECODE_DONE) {
                break;
            }

            switch (index) {
                case 0:
                    t = compositeDecoder.decodeSerializableElement(this.descriptor, 0, this.tSerializer);
                    break;
                default:
                    throw new Error(`Unexpected index: ${index}`);
            }
        }

        compositeDecoder.endStructure(this.descriptor);

        if (t === null) {
            throw new Error("Missing field: t");
        }
        return new AnotherModelHolder<T>({t});
    }

}

export class GenericThing<T1, T2> {
    readonly x: T1
    readonly w: T2[]
    readonly a: string

    constructor({x,w,a}:{x: T1, w: T2[], a: string}) {
        this.x = x
        this.w = w
        this.a = a
    }

}


export class GenericThingSerializer<T1, T2> implements TsSerializer<GenericThing<T1, T2>> {
    private readonly serializer1: TsSerializer<T1>
    private readonly serializer2: TsSerializer<T2>
    descriptor: SerialDescriptor

    constructor(serializer1: TsSerializer<T1>, serializer2: TsSerializer<T2>) {
        this.serializer1 = serializer1
        this.serializer2 = serializer2
        this.descriptor = buildClassSerialDescriptor("Foo", [serializer1.descriptor,serializer2.descriptor], (builder) => {
            builder.element("x", NumberSerializer.descriptor)
            builder.element("w", StringSerializer.descriptor)
            builder.element("a", BooleanSerializer.descriptor)
        })
    }


    serialize(encoder: Encoder, value: GenericThing<T1, T2>) {
        const compositeEncoder = encoder.beginStructure(this.descriptor)
        compositeEncoder.encodeSerializableElement(this.descriptor, 0, this.serializer1, value.x)
        compositeEncoder.encodeSerializableElement(this.descriptor, 1, new ArraySerializer(this.serializer2), value.w)
        compositeEncoder.encodeStringElement(this.descriptor, 2, value.a)
        compositeEncoder.endStructure(this.descriptor)
    }

    deserialize(decoder: Decoder): GenericThing<T1, T2> {
        const compositeDecoder = decoder.beginStructure(this.descriptor);
        let x: T1 | null = null;
        let w: T2[] | null = null;
        let a: string | null = null;

        while (true) {
            const index = compositeDecoder.decodeElementIndex(this.descriptor);
            if (index === DECODER_DECODE_DONE) {
                break;
            }

            switch (index) {
                case 0:
                    x = compositeDecoder.decodeSerializableElement(this.descriptor, 0, this.serializer1);
                    break;
                case 1:
                    w = compositeDecoder.decodeSerializableElement(this.descriptor, 1, new ArraySerializer(this.serializer2),);
                    break;
                case 2:
                    a = compositeDecoder.decodeStringElement(this.descriptor, 2);
                    break;
                default:
                    throw new Error(`Unexpected index: ${index}`);
            }
        }

        compositeDecoder.endStructure(this.descriptor);

        if (x === null) {
            throw new Error("Missing field: x");
        }
        if (w === null) {
            throw new Error("Missing field: y");
        }
        if (a === null) {
            throw new Error("Missing field: z");
        }

        return new GenericThing<T1, T2>({x, w, a});
    }
}


export class Foo {
    readonly x: number
    readonly y: string
    readonly z: boolean

    constructor({x,y,z}:{x: number, y: string, z: boolean}) {
        this.x = x
        this.y = y
        this.z = z
    }
}

const fooDescriptor: SerialDescriptor = buildClassSerialDescriptor("Foo", [], (builder) => {
    builder.element("x", NumberSerializer.descriptor)
    builder.element("y", StringSerializer.descriptor)
    builder.element("z", BooleanSerializer.descriptor)
})
export const FooSerializer: TsSerializer<Foo> = {
    descriptor: fooDescriptor,
    serialize(encoder: Encoder, value: Foo) {
        const compositeEncoder = encoder.beginStructure(fooDescriptor)
        compositeEncoder.encodeNumberElement(fooDescriptor, 0, value.x)
        compositeEncoder.encodeStringElement(fooDescriptor, 1, value.y)
        compositeEncoder.encodeBooleanElement(fooDescriptor, 2, value.z)
        compositeEncoder.endStructure(fooDescriptor)
    },
    deserialize(decoder: Decoder): Foo {
        const compositeDecoder = decoder.beginStructure(fooDescriptor);
        let x: number | null = null;
        let y: string | null = null;
        let z: boolean | null = null;

        while (true) {
            const index = compositeDecoder.decodeElementIndex(fooDescriptor);
            if (index === DECODER_DECODE_DONE) {
                break;
            }

            switch (index) {
                case 0:
                    x = compositeDecoder.decodeNumberElement(fooDescriptor, 0);
                    break;
                case 1:
                    y = compositeDecoder.decodeStringElement(fooDescriptor, 1);
                    break;
                case 2:
                    z = compositeDecoder.decodeBooleanElement(fooDescriptor, 2);
                    break;
                default:
                    throw new Error(`Unexpected index: ${index}`);
            }
        }

        compositeDecoder.endStructure(fooDescriptor);

        if (x === null) {
            throw new Error("Missing field: x");
        }
        if (y === null) {
            throw new Error("Missing field: y");
        }
        if (z === null) {
            throw new Error("Missing field: z");
        }

        return new Foo({x, y, z});
    }
}