import {FetchRpcClient} from "../src/runtime/components/FetchRpcClient";
import {JsonFormat} from "../src/runtime/components/JsonFormat";
import {UserProtocolApi} from "./generated/UserProtocolApi";

test("Codegened Client works", async () => {
    const client = new UserProtocolApi(new FetchRpcClient("http://localhost:8080"), JsonFormat)
    const res = await client.createLobby({num: 2}, "asdf")
    expect(res).toEqual({id: 6})
})

test("Codegened Client works in more cases", async () => {
    const client = new UserProtocolApi(new FetchRpcClient("http://localhost:8080"), JsonFormat)
    const res = await client.test([1, 2])
    expect(res).toEqual([[1, 2, "3"], 4])
})
interface Foo {
    x : void
}

const y: Foo = {
    x: undefined
}

test("Codegened Client works in all cases", async () => {
    const client = new UserProtocolApi(new FetchRpcClient("http://localhost:8080"), JsonFormat)
    const res = await client.test([1, 2])
    expect(res).toEqual([[1, 2, "3"], 4])

    expect(await client.createLobby({num: 123}, "alo"))
        .toEqual({id: 126})

    expect(await client.killSomeone(111, {num: 5})).toEqual(116)

    await client.someShit(1, 2);
    await client.someShit(1, 2);
    await client.moreTypes(
        [],
        [],
        [1, 2],
        [undefined, {num: 1}, ""],
        [1,1]
    );
    //
    // let result = protocol.test([1, 2]);
    // if (result[0] !== [1, 2, "3"] || result[1] !== 4.0) {
    //     throw new Error("Assertion failed");
    // }
    //
    // protocol.nullable(null, [null]);
    //
    // console.log("fioi");
    // console.log("fioi");
    //
    // protocol.someShit(1, 2);
    // protocol.genericTest("");
})

test("Randomthing", () => {
    const js = `{"foo": "bar}`
    const obj = {
        x: 2,
        y: {
            z: {
                thing: "void"
            }
        }
    }
    JSON.stringify(obj, (key, value) => {
        console.log(`Key: ${key}, value: ${value}`)
        return value
    })
    // @ts-ignore
    console.log(js["foo"])
})

//
//     @Test
//     fun testNullableTypes(): Unit = runBlocking {
//         val protocol = userExtension.api
//         expectThat(protocol.heavyNullable(UserProtocol.HeavyNullableTestMode.EntirelyNull)).isEqualTo(null)
//         expectThat(protocol.heavyNullable(UserProtocol.HeavyNullableTestMode.NullList)).isEqualTo(GenericThing(null, null, listOf()))
//         expectThat(protocol.heavyNullable(UserProtocol.HeavyNullableTestMode.NullString)).isEqualTo(
//             GenericThing(
//                 listOf(null, "test"),
//                 null,
//                 listOf()
//             )
//         )
//     }
//
//     @Test
//     fun testManual(): Unit = runBlocking {
//         val protocol = simpleExtension.api
//         val response = protocol.bar(2)
//         assertEquals(3, response)
//     }
//
//     @Test
//     fun testExceptions(): Unit = runBlocking {
//         expectThrows<RpcResponseException> {
//             userExtension.api.errorTest()
//         }.get { code == 500 }
//
//         expectThrows<RpcResponseException> {
//             userExtension.api.requirementTest()
//         }.get { code == 400 }
//     }
//
//     @Test
//     fun testExoticTypes(): Unit = runBlocking {
//         val y = "Asdf"
//         val protocol = userExtension.api
//         expectThat(protocol.withNullsTest(WithNulls(listOf("2", null), y = y)))
//             .isEqualTo(WithNulls(listOf(1, null), y))
//
//         expectThat(protocol.enumArgsTest(EnumArgs.Option1)).isEqualTo(EnumArgs.Option5)
//
//         expectThat(protocol.directObjectTest(TheObject)).isEqualTo(TheObject)
//
//         val thing: PolymorphicThing = PolymorphicThing.Option2
//         expectThat(protocol.polymorphicTest(thing)).isEqualTo(thing)
//         val direct: PolymorphicThing.Option1 = PolymorphicThing.Option1(2)
//         expectThat(protocol.directPolymorphicAccess(direct)).isEqualTo(direct)
//         val polymorphicClass = PolymorphicClass.Option4(3)
//         expectThat(protocol.polymorphicClassTest(polymorphicClass)).isEqualTo(polymorphicClass)
//         val everything = EveryBuiltinType(
//             false, 1, 2, 3, 4, '5', "6",
//             byteArrayOf(7), ShortArray(8), IntArray(9), longArrayOf(10), charArrayOf('@'),
//             listOf(11), mapOf(12 to 13), setOf(14), 15 to 16, Triple(17, 18, 19), Unit,
//             arrayOf(20), ubyteArrayOf(21u), ushortArrayOf(22u), uintArrayOf(23u), ULongArray(24), 25u, 26u, 27u, 28u,29f,30.0
//
//         )
//         expectThat(protocol.everyBuiltinType(everything)).isEqualTo(everything)
//         expectThat(
//             protocol.everyBuiltinTypeParams(
//                 false, 1, 2, 3, 4, '5', "6",
//                 byteArrayOf(7), ShortArray(8), IntArray(9), longArrayOf(10), charArrayOf('@'),
//                 listOf(11), mapOf(12 to 13), setOf(14), 15 to 16, Triple(17, 18, 19), Unit,
//                 arrayOf(20), ubyteArrayOf(21u), ushortArrayOf(22u), uintArrayOf(23u), ULongArray(24), 25u, 26u, 27u, 28u,29f,30.0
//             )
//         ).isEqualTo(Triple(17, 18, 19))
//     }

// Triple(1, 2, "3") to 4.0

//TODO: type: in struct unions
//TODO: in kotlin, serialize map.entry, pair and triple as arrays.