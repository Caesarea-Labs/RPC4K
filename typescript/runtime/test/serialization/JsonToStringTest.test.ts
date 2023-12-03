import {Json} from "../../src/serialization/json/Json";
import {NumberSerializer, StringSerializer} from "../../src/serialization/BuiltinSerializers";
import {JsonConfiguration} from "../../src/serialization/json/JsonConfiguration";
import {
    AnotherModelHolder,
    Foo,
    GenericThing, Rpc4tsSerializers, Tree
} from "./TestModels";

test("Test toString", () => {
    const obj = new Foo({x: 1, y: "hello", z: false})
    const json = new Json(new JsonConfiguration(), {})

    const res = json.encodeToString(Rpc4tsSerializers.foo(), obj)

    expect(res).toEqual(`{"x":1,"y":"hello","z":false}`)

    console.log(res)
})

test("Test GenericThing serialization to string", () => {
    const obj = new GenericThing({x:1,w: ["Asdf"],a: "3"})
    const json = new Json(new JsonConfiguration(), {})
    const res = json.encodeToString(Rpc4tsSerializers.genericThing(NumberSerializer, StringSerializer), obj)
    expect(res).toEqual(`{"x":1,"w":["Asdf"],"a":"3"}`)
})

test("Test AnotherModelHolder serialization to string", () => {
    const obj = new AnotherModelHolder({t: new GenericThing({x: 1, w: ["Asdf"], a: "3"})})
    const json = new Json(new JsonConfiguration(), {})
    const res = json.encodeToString(Rpc4tsSerializers.anotherModelHolder(NumberSerializer), obj)
    expect(res).toEqual(`{"t":{"x":1,"w":["Asdf"],"a":"3"}}`)
})
test("Test Tree serialization to string", () => {
    const obj: Tree<string> =  {
        item: "test",
        children: [
            {
                item: "foo",
                children: [
                    {
                        item: "Bar",
                        children: []
                    }
                ]
            },
            {
                item: "Biz",
                children: []
            }
        ]
    }
    const json = new Json(new JsonConfiguration(), {})
    const res = json.encodeToString(Rpc4tsSerializers.rpc4ts_serializer_Tree(StringSerializer), obj)
    expect(res).toEqual(`{"item":"test","children":[{"item":"foo","children":[{"item":"Bar","children":[]}]},{"item":"Biz","children":[]}]}`)
})
