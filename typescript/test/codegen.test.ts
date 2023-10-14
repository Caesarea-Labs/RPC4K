import {CodeBuilder} from "../src/runtime/generator/CodeBuilder";

test("Test codegen", () => {
    const dog = new CodeBuilder().addInterface("Dog", builder => {
        builder.addProperty("name", "string")
            .addProperty("type", "string")
            .addProperty("age", "number")
    }).build()
    console.log(dog)

    const myApiClientImpl = new CodeBuilder().addClass("MyApiClientImpl", clazz => {
        clazz.addProperty("private readonly client", "RpcClient")
            .addProperty("private readonly format", "SerializationFormat")
            .addConstructor([["client", "RpcClient"], ["format", "SerializationFormat"]], constructor => {
                constructor.addAssignment("this.client", "client")
                    .addAssignment("this.format", "format")
            })
            .addFunction("getDogs", [["num", "number"], ["type", "string"]], "Promise<Dog[]>",func => {
                func.addReturningFunctionCall("GeneratedCodeUtils.request",["this.client", "this.format", `"getDogs"`, "num", "type"])
            })
            .addFunction("putDog", [["dog", "Dog"]], "Promise<void>",func => {
                func.addReturningFunctionCall("GeneratedCodeUtils.request",["this.client", "this.format", `"putDog"`, "dog"])
            })
    }).build()
    console.log(myApiClientImpl)
})

// class MyApiClientImpl {
//     private readonly client: RpcClient
//     private readonly format: SerializationFormat
//
//     constructor(client: RpcClient, format: SerializationFormat) {
//         this.client = client
//         this.format = format
//     }
//
//     getDogs(num: number, type: string): Promise<Dog[]> {
//         return GeneratedCodeUtils.request(this.client, this.format, "getDogs", num, type)
//     }
//
//     putDog(dog: Dog): Promise<void> {
//         return GeneratedCodeUtils.request(this.client, this.format, "putDog", dog)
//     }
// }

//interface Dog {
//     name: string
//     type: string
//     age: number
// }