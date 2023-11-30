import {Json} from "../../src/serialization/json/Json";
import {TsSerializer} from "../../src/serialization/core/TsSerializer";
import {BooleanSerializer, NumberSerializer, StringSerializer} from "../../src/serialization/builtins/BuiltinSerializers";
import {Encoder} from "../../src/serialization/core/encoding/Encoder";
import {Decoder, DECODER_DECODE_DONE} from "../../src/serialization/core/encoding/Decoding";
import {JsonConfiguration} from "../../src/serialization/json/JsonConfiguration";
import {buildClassSerialDescriptor} from "../../src/serialization/descriptors/SerialDescriptors";
import {SerialDescriptor} from "../../src/serialization/core/SerialDescriptor";
import {Foo, FooSerializer} from "./JsonToStringTest.test";

test("Test fromString", () => {
    const string = `{"x":1,"y":"hello","z":false}`
    const obj = new Foo(1, "hello", false)
    const json = new Json(new JsonConfiguration(), {})

    const res = json.decodeFromString(FooSerializer, string)

    expect(res).toEqual(obj)

    console.log(res)
})
