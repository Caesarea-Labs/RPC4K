
import {CodeBuilder} from "../src/codegen/CodeBuilder";
import {TsTypes} from "../src/codegen/FormatString";
const RPC_CLIENT = TsTypes.library("RpcClient", "RpcClient")
const SERIALIZATION_FORMAT = TsTypes.library("SerializationFormat", "SerializationFormat")
const DOG = TsTypes.create("Dog", "Dog")
// const PROMISE = TsTypes.("SerializationFormat", "SerializationFormat")

test("Test codegen", () => {
    const dog = new CodeBuilder(false).addInterface({name: "Dog"}, builder => {
        builder.addProperty({name: "name", type: TsTypes.STRING })
            .addProperty({name: "type", type: TsTypes.STRING })
            .addProperty({name: "age", type: TsTypes.NUMBER})
    }).build()
    console.log(dog)

    const myApiClientImpl = new CodeBuilder(false).addClass({name:"MyApiClientImpl"}, clazz => {
        clazz.addProperty({name: "private readonly client", type: RPC_CLIENT })
            .addProperty({name: "private readonly format", type: SERIALIZATION_FORMAT })
            .addConstructor([["client",RPC_CLIENT], ["format",SERIALIZATION_FORMAT]], constructor => {
                constructor.addAssignment("this.client", "client")
                    .addAssignment("this.format", "format")
            })
            .addFunction("getDogs", [["num", TsTypes.NUMBER], ["type", TsTypes.STRING]], TsTypes.promise(DOG), func => {
                func.addReturningFunctionCall("GeneratedCodeUtils.request", ["this.client", "this.format", `"getDogs"`, "num", "type"])
            })
            .addFunction("putDog", [["dog", DOG]], TsTypes.promise(TsTypes.VOID), func => {
                func.addReturningFunctionCall("GeneratedCodeUtils.request", ["this.client", "this.format", `"putDog"`, "dog"])
            })
    }).build()
    console.log(myApiClientImpl)
})

// test("Test apidefinition codegen", () => {
//     generateClientModel(TestRpcJson,"test/generated/", {localLibPaths: true})
// })
