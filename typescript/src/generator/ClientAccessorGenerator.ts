import {ApiDefinition, RpcParameter, RpcType} from "./ApiDefinition";
import {CodeBuilder} from "./CodeBuilder";
import {isBuiltinType, typescriptRpcType} from "./TypescriptRpcType";
import {Rpc4TsClientGenerationOptions} from "./ClientGenerator";

export function generateAccessor(api: ApiDefinition, options: Rpc4TsClientGenerationOptions): string {
    const builder = new CodeBuilder()

    function libraryPath(path: string): string {
        if (options.localLibPaths) return `../../src/runtime/${path}`
        else {
            throw new Error("Not implemented yet - need to see what the file structure is")
        }
    }

    builder.addImport(["RpcClient"], libraryPath("RpcClient"))
        .addImport(["SerializationFormat"], libraryPath("SerializationFormat"))
        .addImport(["GeneratedCodeUtils"], libraryPath("impl/GeneratedCodeUtils"))

    builder.addImport(getReferencedGeneratedTypeNames(api), `./${api.name}Models`)

    builder.addClass(`${api.name}Api`, (clazz) => {
        clazz.addProperty({name: "private readonly client", type: "RpcClient"})
            .addProperty({name: "private readonly format", type: "SerializationFormat"})
            .addConstructor([["client", "RpcClient"], ["format", "SerializationFormat"]], constructor => {
                constructor.addAssignment("this.client", "client")
                    .addAssignment("this.format", "format")
            })

        for (const method of api.methods) {
            // We wrap the return type with a promise because api methods are network calls
            const returnType: RpcType = {
                name: "Promise",
                isOptional: false,
                typeArguments: [method.returnType],
                isTypeParameter: false,
                inlinedType: null
            }
            clazz.addFunction(
                method.name,
                method.parameters.map(param => [param.name, typescriptRpcType(param.type)]),
                typescriptRpcType(returnType),
                (body) => {
                    body.addReturningFunctionCall(
                        "GeneratedCodeUtils.request",
                        // Add all the parameters as a trailing argument to GeneratedCodeUtils.request
                        [
                            "this.client", "this.format", `"${method.name}"`,
                            ...method.parameters.map(param => paramValue(param))
                        ]
                    )
                })
        }
    })

    return builder.build()
}

function paramValue(param: RpcParameter): string {
    // RPC4All defines that the value of the void type should always be "void"
    if (param.type.name === "void") return `"void"`
    else return param.name
}

/**
 * Gets all the custom generated classes that the api client accessor referenced so we can import them
 */
function getReferencedGeneratedTypeNames(api: ApiDefinition): string[] {
    const names: Set<string> = new Set()
    for (const method of api.methods) {
        for (const param of method.parameters) {
            addReferencedGeneratedTypeNames(param.type, names)
        }
        addReferencedGeneratedTypeNames(method.returnType, names)
    }
    return Array.from(names)
}

function addReferencedGeneratedTypeNames(type: RpcType, addTo: Set<string>) {
    if (type.inlinedType !== null) {
        addReferencedGeneratedTypeNames(type.inlinedType, addTo)
    } else {
        if (!isBuiltinType(type)) {
            addTo.add(type.name)
        }
        for (const typeArgument of type.typeArguments) {
            addReferencedGeneratedTypeNames(typeArgument, addTo)
        }
    }
}
