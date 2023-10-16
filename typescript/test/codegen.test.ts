
import {TestRpcJson} from "../src/TestRpcJson";
import {CodeBuilder} from "../src/generator/CodeBuilder";
import {generateClientModel} from "../src/generator/ClientGenerator";

test("Test codegen", () => {
    const dog = new CodeBuilder().addInterface({name: "Dog"}, builder => {
        builder.addProperty({name: "name", type: "string"})
            .addProperty({name: "type", type: "string"})
            .addProperty({name: "age", type: "number"})
    }).build()
    console.log(dog)

    const myApiClientImpl = new CodeBuilder().addClass("MyApiClientImpl", clazz => {
        clazz.addProperty({name: "private readonly client", type: "RpcClient"})
            .addProperty({name: "private readonly format", type: "SerializationFormat"})
            .addConstructor([["client", "RpcClient"], ["format", "SerializationFormat"]], constructor => {
                constructor.addAssignment("this.client", "client")
                    .addAssignment("this.format", "format")
            })
            .addFunction("getDogs", [["num", "number"], ["type", "string"]], "Promise<Dog[]>", func => {
                func.addReturningFunctionCall("GeneratedCodeUtils.request", ["this.client", "this.format", `"getDogs"`, "num", "type"])
            })
            .addFunction("putDog", [["dog", "Dog"]], "Promise<void>", func => {
                func.addReturningFunctionCall("GeneratedCodeUtils.request", ["this.client", "this.format", `"putDog"`, "dog"])
            })
    }).build()
    console.log(myApiClientImpl)
})

test("Test apidefinition codegen", () => {
    generateClientModel(TestRpcJson,"test/generated/", {localLibPaths: true})
})
