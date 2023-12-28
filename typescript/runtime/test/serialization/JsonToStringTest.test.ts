import {Json} from "../../src/serialization/json/Json";
import {NumberSerializer, StringSerializer} from "../../src/serialization/BuiltinSerializers";
import {JsonConfiguration} from "../../src/serialization/json/JsonConfiguration";
import {
    AnotherModelHolder,
    Foo,
    GenericThing, Rpc4tsSerializers, TestEnum, TestUnion, Tree
} from "./TestModels";

test("Test toString", () => {
    const obj = ({x: 1, y: "hello", z: false})
    const json = new Json(new JsonConfiguration())

    const res = json.encodeToString(Rpc4tsSerializers.foo(), obj)

    expect(res).toEqual(`{"x":1,"y":"hello","z":false}`)

    console.log(res)
})

test("Test GenericThing serialization to string", () => {
    const obj = ({x:1,w: ["Asdf"],a: "3"})
    const json = new Json(new JsonConfiguration())
    const res = json.encodeToString(Rpc4tsSerializers.genericThing(NumberSerializer, StringSerializer), obj)
    expect(res).toEqual(`{"x":1,"w":["Asdf"],"a":"3"}`)
})

test("Test AnotherModelHolder serialization to string", () => {
    const obj = ({t: ({x: 1, w: ["Asdf"], a: "3"})})
    const json = new Json(new JsonConfiguration())
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
    const json = new Json()
    const res = json.encodeToString(Rpc4tsSerializers.rpc4ts_serializer_Tree(StringSerializer), obj)
    expect(res).toEqual(`{"item":"test","children":[{"item":"foo","children":[{"item":"Bar","children":[]}]},{"item":"Biz","children":[]}]}`)
})

test("Test Enum serialization to string", () => {
    const string =  `"a"`
    const enumValue: TestEnum = "a"
    const json = new Json()
    const res = json.encodeToString(Rpc4tsSerializers.testEnum(), enumValue)
    expect(res).toEqual(string)
})


test("Test Polymorphic serialization to string", () => {
    const obj: TestUnion<string> = ({
        t: ({
            x: "Sdf",
            w: ["123"],
            a :"we"
        })
    })
    const string =  `{"type":"AnotherModelHolder","t":{"x":"Sdf","w":["123"],"a":"we"}}`
    const json = new Json()
    const res = json.encodeToString(Rpc4tsSerializers.testUnion(StringSerializer), obj)
    expect(res).toEqual(string)
})

