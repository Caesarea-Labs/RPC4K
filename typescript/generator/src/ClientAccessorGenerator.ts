import {CodeBuilder} from "./codegen/CodeBuilder";
import {Rpc4TsClientGenerationOptions} from "./ClientGenerator";
import {ApiDefinition, RpcType, RpcTypeNames} from "rpc4ts-runtime";
import {isBuiltinType, modelName, typescriptRpcType} from "./Rpc4tsType";
import {addSerializerImports, buildSerializer, libraryPath, serializerName} from "./SerializerGenerator";

/**
 * @param api definition with default values
 * @param rawApi definition without default values
 */
export function generateAccessor(api: ApiDefinition, rawApi: ApiDefinition, options: Rpc4TsClientGenerationOptions): string {
    const builder = new CodeBuilder()


    const referencedModels = api.models.filter(model => model.type !== "inline")
        .map(model => modelName(model.name))

    builder.addImport(["RpcClient"], libraryPath("RpcClient", options))
        .addImport(["SerializationFormat"], libraryPath("SerializationFormat", options))
        .addImport(["GeneratedCodeUtils"], libraryPath("impl/GeneratedCodeUtils", options))
        // .addImport(["Rpc4aTypeAdapter"], libraryPath("impl/Rpc4aTypeAdapter", options))
        .addImport(getReferencedGeneratedTypeNames(api), `./rpc4ts_${api.name}Models`)
        // .addImport(["UserProtocolRuntimeModels"], `./${api.name}RuntimeModels`)
        .addImport(["Dayjs"], `dayjs`)
        .addImport(["Duration"], `dayjs/plugin/duration`)
        .addImport(referencedModels.map(model => serializerName(model)), `./rpc4ts_${api.name}Serializers`)

    addSerializerImports(builder, options)

    const runtimeModelsName = `${api.name}RuntimeModels`

    builder._addLineOfCode(`const ${runtimeModelsName} = \`${JSON.stringify(rawApi.models)}\``)

    builder.addClass({name: `${api.name}Api`}, (clazz) => {
        clazz.addProperty({name: "private readonly client", type: "RpcClient"})
            .addProperty({name: "private readonly format", type: "SerializationFormat"})
            // .addProperty({
            //     name: "private readonly adapter",
            //     type: "Rpc4aTypeAdapter",
            //     initializer: `GeneratedCodeUtils.createTypeAdapter(${runtimeModelsName})`
            // })
            .addConstructor([["client", "RpcClient"], ["format", "SerializationFormat"]], constructor => {
                constructor.addAssignment("this.client", "client")
                    .addAssignment("this.format", "format")
            })

        for (const method of api.methods) {
            // We wrap the return type with a promise because api methods are network calls
            const returnType: RpcType = {
                name: "Promise",
                isNullable: false,
                typeArguments: [method.returnType],
                isTypeParameter: false,
                inlinedType: undefined
            }
            const returnTypeString = typescriptRpcType(returnType)
            clazz.addFunction(
                method.name,
                method.parameters.map(param => [param.name, typescriptRpcType(param.type)]),
                returnTypeString,
                (body) => {
                    const args = [
                        "this.client", "this.format", `"${method.name}"`,
                        arrayLiteral(method.parameters.map(param => param.name)),
                        // Get rid of default values to be the type that is manually inputted be shorter
                        arrayLiteral(method.parameters.map(param => buildSerializer(param.type, {})))
                    ]
                    const isVoid = method.returnType.name === RpcTypeNames.Void
                    if (!isVoid) {
                        const retvalSerializer = buildSerializer(method.returnType, {})
                        // Add return type if it's not null
                        args.push(retvalSerializer)
                    }
                    const functionName = isVoid ? "send" : "request"
                    body.addReturningFunctionCall(
                        `GeneratedCodeUtils.${functionName}`,
                        // Add all the parameters as a trailing argument to GeneratedCodeUtils.request
                        args,
                        false
                    )
                    if (method.returnType.name === RpcTypeNames.Tuple) {
                        // The type for tuples is not recognized correctly so we need to explicitly cast it
                        body.addCast(returnTypeString)
                    }
                    body.addEmptyLine()
                })
        }
    })

    return builder.build()
}

function stringifyToJsObject(obj: unknown): string {
    return JSON.stringify(obj).replace(/"(\w+)"\s*:/g, '$1:')
}

function arrayLiteral(list: string[]): string {
    return "[" + list.join(", ") + "]"
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
    // if (type.inlinedType !== undefined) {
    //     addReferencedGeneratedTypeNames(type.inlinedType, addTo)
    // } else {
    if (!isBuiltinType(type)) {
        addTo.add(modelName(type.name))
    }
    for (const typeArgument of type.typeArguments) {
        addReferencedGeneratedTypeNames(typeArgument, addTo)
    }
    // }
}
