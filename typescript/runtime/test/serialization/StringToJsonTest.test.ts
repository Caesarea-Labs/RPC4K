import {Json} from "../../src/serialization/json/Json";
import {NumberSerializer, StringSerializer} from "../../src/serialization/BuiltinSerializers";
import {JsonConfiguration} from "../../src/serialization/json/JsonConfiguration";
import {
    AnotherModelHolder,
    Foo,
    GenericThing,
    Rpc4tsSerializers, Tree
} from "./TestModels";

test("Test fromString", () => {
    const string = `{"x":1,"y":"hello","z":false}`
    const obj = new Foo({x:1,y: "hello",z: false})
    const json = new Json(new JsonConfiguration(), {})

    const res = json.decodeFromString(Rpc4tsSerializers.foo(), string)

    expect(res).toEqual(obj)

    console.log(res)
})


test("Test GenericThing serialization from string", () => {
    const string = `{"x":1,"w":["Asdf"],"a":"3"}`
    const obj = new GenericThing({x: 1, w: ["Asdf"], a: "3"})
    const json = new Json(new JsonConfiguration(), {})
    const res = json.decodeFromString(Rpc4tsSerializers.genericThing(NumberSerializer, StringSerializer), string)
    expect(res).toEqual(obj)
})

//TODO: consider adding special constructor that make it easier to create nested objects
// instead of:
// AnotherModelHolder (t: new GenericThing({...}) )
// do:
// AnotherModelHolder (t: {...} )

test("Test AnotherModelHolder serialization from string", () => {
    const string = `{"t":{"x":1,"w":["Asdf"],"a":"3"}}`
    const obj = new AnotherModelHolder({t: new GenericThing({x: 1, w: ["Asdf"], a: "3"})})
    const json = new Json(new JsonConfiguration(), {})
    const res = json.decodeFromString( Rpc4tsSerializers.anotherModelHolder(NumberSerializer), string)
    expect(res).toEqual(obj)
})

test("Test Tree serialization to string", () => {
    const string = `{"item":"test","children":[{"item":"foo","children":[{"item":"Bar","children":[]}]},{"item":"Biz","children":[]}]}`
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
    const res = json.decodeFromString(Rpc4tsSerializers.rpc4ts_serializer_Tree(StringSerializer), string)
    expect(res).toEqual(obj)
})

