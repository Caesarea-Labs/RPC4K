import {FetchRpcClient, JsonFormat} from "../src/components";
import dayjs from "dayjs";
import {AllEncompassingServiceApi} from "./generated/rpc4ts_AllEncompassingServiceApi";
import {
    CreateLobbyResponse,
    EnumArgsValues,
    EveryBuiltinType,
    GenericThing, InlineHolder2,
    MutableThings, PlayerId,
    PolymorphicThing,
    PolymorphicThingOption1, PolymorphicThingOption2, SomeBuiltinTypes, SomeMap, TheObject, TypeField, WithNulls
} from "./generated/rpc4ts_AllEncompassingServiceModels";
import {PolymorphicClassOption4} from "./generated/rpc4ts_AllEncompassingServiceModels";
import JestMatchers = jest.JestMatchers;
import {RpcResponseError} from "../src/RpcClientError";
import duration from "dayjs/plugin/duration";
import {NodejsWebsocket} from "../src/components/NodejsWebsocket"
import {GeneratedCodeUtils} from "../src/impl/GeneratedCodeUtils"
import {StringSerializer} from "../src/serialization/BuiltinSerializers"
import {Observable} from "../src/Observable";


test("Codegened Client works", async () => {
    const client = new AllEncompassingServiceApi(new FetchRpcClient("http://localhost:8080", new NodejsWebsocket("http://localhost:8080/events")), JsonFormat)
    const res = await client.createLobby(({num: 2}), "asdf")
    expect(res).toEqual(({id: 6}))
})

test("Codegened Client works in more cases", async () => {
    const client = new AllEncompassingServiceApi(new FetchRpcClient("http://localhost:8080", new NodejsWebsocket("http://localhost:8080/events")), JsonFormat)
    const res = await client.test([1, 2])
    expect(res).toEqual([[1, 2, "3"], 4])
})

test("Test enum values", () => {
    expect(EnumArgsValues).toEqual(["Option1", "Option5"])
})

//TODO: add a more complex event test with complex models

test("Test events", async () => {
    const fetch = new FetchRpcClient("http://localhost:8080", new NodejsWebsocket("http://localhost:8080/events"))
    const format = JsonFormat
    const client = new AllEncompassingServiceApi(fetch,format)

    let actualMessage: string | null = null
    client.eventTest("test").observe(value => {
        actualMessage = value
    })
    await wait(1000)

    await client.tinkerWithEvents()

    await wait(1000)

    expect(actualMessage).toEqual("test5")
})

test("Complex events", async () => {
    const fetch = new FetchRpcClient("http://localhost:8080", new NodejsWebsocket("http://localhost:8080/events"))
    const format = JsonFormat
    const client = new AllEncompassingServiceApi(fetch,format)

    let actualMessage: TypeField | null = null
    client.complexEventTest(["foo", {type: "23", other: 4}],{type: "414"}).observe(value => {
        actualMessage = value
    })
    await wait(1000)

    await client.invokeComplexEventTest()

    await wait(1000)

    expect(actualMessage).toEqual({type: "414"})
})


function wait(delay: number): Promise<void> {
    return new Promise(exec => {
        setTimeout(exec, delay)
    })
}

test("Codegened Client works in all cases", async () => {
    const client = new AllEncompassingServiceApi(new FetchRpcClient("http://localhost:8080", new NodejsWebsocket("http://localhost:8080/events") ), JsonFormat)
    const res = await client.test([1, 2])
    expect(res).toEqual([[1, 2, "3"], 4])

    expect(await client.createLobby(({num: 123}), "alo"))
        .toEqual({id: 126})

    expect(await client.killSomeone(111, ({num: 5}))).toEqual(116)

    await client.someShit(1, 2);
    await client.someShit(1, 2);
    await client.moreTypes(
        [],
        [],
        [1, 2],
        [undefined, ({num: 1}), ""],
        [1, 1]
    );

    const result = await client.test([1, 2]);

    expect(result[0]).toEqual([1, 2, "3"])
    expect(result[1]).toEqual(4.0)

    await client.nullable(null, [null]);

    await client.someShit(1, 2);
    await client.genericTest("");

    expect(await client.heavyNullable("EntirelyNull")).toEqual<NullableArgType>(null)
    expect(await client.heavyNullable("NullList")).toEqual<NullableArgType>(({x: null, y: null, z: [], w: []}))
    expect(await client.heavyNullable("NullString")).toEqual<NullableArgType>(({x: [null, "test"], y: null, z: [], w: []}))

    const error1 = (await expectThrows(() => client.errorTest(), RpcResponseError))
    error1.toHaveProperty("code", 500)

    const error2 = (await expectThrows(() => client.requirementTest(), RpcResponseError))
    error2.toHaveProperty("code", 400)

    const y = "Asdf"
    expect(await client.withNullsTest(({x: ["2", null], y}))).toEqual({x: [1, null], y});

    expect(await client.enumArgsTest("Option1")).toEqual("Option5");

    expect(await client.directObjectTest(({}))).toEqual({});

    const thing: PolymorphicThing = ({type: "Option2"})
    expect(await client.polymorphicTest(thing)).toEqual(thing);

    const direct: PolymorphicThingOption1 = ({x: 2, type: "Option1"})
    expect(await client.directPolymorphicAccess(direct)).toEqual(direct);

    const polymorphicClass: PolymorphicClassOption4 = ({x: 3, type: "Option4"})
    expect(await client.polymorphicClassTest(polymorphicClass)).toEqual(polymorphicClass);


    expect(await client.someBuiltinTypes(({p: [15, 16]}))).toEqual({p: [15, 16]})

    expect(await client.someMap(({map: {1: 3.4}}))).toEqual(({map: {1: 3.4}}))

    const everything: EveryBuiltinType = ({
        // Adjusted with simplified array literals
        a: false, b: 1, c: 2, d: 3, e: 4, f: '5', g: "6",
        h: [7], i: [8], j: [9], k: [10], l: ['@'],
        m: [11], n: {12: 13}, o: [14], p: [15, 16], q: [17, 18, 19],
        r: undefined, s: [21], t: [22], u: [23], v: [24], w: [25], x: 26, y: 27, z: 28, a2: 29.0, b2: 30.0, c2: 31, d2: [32, 33],
        e2: dayjs(), f2: dayjs(), g2: "ffffffff-ffff-ffff-ffff-ffffffffffff", h2: dayjs.duration(34, "seconds")
    });
    const everythingBack = await client.everyBuiltinType(everything)
    // Turn the duration into an iso string because equality doesn't work well on the object itself
    expect({...everythingBack, h2: everythingBack.h2.toISOString()}).toEqual({...everything, h2: everything.h2.toISOString()});

    expect(
        await client.everyBuiltinTypeParams(
            false, 1, 2, 3, 4, '5', "6",
            [7], [8], [9], [10], ['@'],
            [11], {12: 13}, [14], [15, 16], [17, 18, 19],
            undefined, [21], [22], [23], [24], [25],
            26, 27, 28, 29.0, 30.0, 31, [32, 33], dayjs(), dayjs(), "ffffffff-ffff-ffff-ffff-ffffffffffff",
            dayjs.duration(34, "seconds")
        )
    ).toEqual([17, 18, 19]);

    expect(await client.returningDataEnum("Option1")).toEqual("Option1")

    expect(await client.inlineHolder(({value: 2}))).toEqual({value: 2})
    expect(await client.typeField(({type: "wef"}))).toEqual({type: "wef"})

    expect(await client.nullDate(null)).toEqual(null)

    const mutable: MutableThings = ({
        map: {
            "1": 2
        },
        list: [3],
        set: [4]
    })
    expect(await client.mutableThings(mutable)).toEqual(mutable)
    expect(await client.mutableThingsAsParams({"1": 2}, [3])).toEqual([1, 2, 3])

    expect(await client.largeHierarchy(({x: 1, type: "Option1"}))).toEqual(({x: 1, type: "Option1"}))

    //NiceToHave: Respect @SerialName
    // expect(await client.serialName({}))
})

async function expectThrows<T extends Error>(code: () => Promise<void>, error: Constructable2<T>): Promise<JestMatchers<T>> {
    try {
        await code()
    } catch (e) {
        if (!(e instanceof error)) throw new Error(`Code threw, but the incorrect type: ${e}`)
        return expect(e)
    }
    throw new Error(`Code did not throw.`)
}

interface Constructable2<T> {
    new(...args: never[]): T;
}

type NullableArgType = GenericThing<(string | null)[] | null, string[] | null, (string | null)[]> | null

