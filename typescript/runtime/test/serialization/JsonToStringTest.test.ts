import {Json} from "../../src/serialization/json/Json";
import {TsSerializer} from "../../src/serialization/core/TsSerializer";
import {BooleanSerializer, NumberSerializer, StringSerializer} from "../../src/serialization/builtins/BuiltinSerializers";
import {Encoder} from "../../src/serialization/core/encoding/Encoder";
import {Decoder, DECODER_DECODE_DONE} from "../../src/serialization/core/encoding/Decoding";
import {JsonConfiguration} from "../../src/serialization/json/JsonConfiguration";
import {buildClassSerialDescriptor} from "../../src/serialization/descriptors/SerialDescriptors";
import {SerialDescriptor} from "../../src/serialization/core/SerialDescriptor";

test("Test toString", () => {
    const obj = new Foo(1, "hello", false)
    const json = new Json(new JsonConfiguration(), {})

    const res = json.encodeToString(FooSerializer, obj)

    expect(res).toEqual(`{"x":1,"y":"hello","z":false}`)

    console.log(res)
})

export class Foo {
    readonly x: number
    readonly y: string
    readonly z: boolean

    constructor(x: number, y: string, z: boolean) {
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

        return new Foo(x, y, z);
    }
}