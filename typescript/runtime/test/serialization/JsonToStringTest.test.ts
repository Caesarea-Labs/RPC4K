import {Json} from "../../src/serialization/json/Json";
import {NumberSerializer, StringSerializer} from "../../src/serialization/builtins/BuiltinSerializers";
import {JsonConfiguration} from "../../src/serialization/json/JsonConfiguration";
import {AnotherModelHolder, AnotherModelHolderSerializer, Foo, FooSerializer, GenericThing, GenericThingSerializer} from "./TestModels";

test("Test toString", () => {
    const obj = new Foo({x: 1, y: "hello", z: false})
    const json = new Json(new JsonConfiguration(), {})

    const res = json.encodeToString(FooSerializer, obj)

    expect(res).toEqual(`{"x":1,"y":"hello","z":false}`)

    console.log(res)
})

test("Test GenericThing serialization to string", () => {
    const obj = new GenericThing({x:1,w: ["Asdf"],a: "3"})
    const json = new Json(new JsonConfiguration(), {})
    const res = json.encodeToString(new GenericThingSerializer(NumberSerializer, StringSerializer), obj)
    expect(res).toEqual(`{"x":1,"w":["Asdf"],"a":"3"}`)
})

test("Test AnotherModelHolder serialization to string", () => {
    const obj = new AnotherModelHolder({t: new GenericThing({x: 1, w: ["Asdf"], a: "3"})})
    const json = new Json(new JsonConfiguration(), {})
    const res = json.encodeToString(new AnotherModelHolderSerializer(NumberSerializer), obj)
    expect(res).toEqual(`{"t":{"x":1,"w":["Asdf"],"a":"3"}}`)
})
