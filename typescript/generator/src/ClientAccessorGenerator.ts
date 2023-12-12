import {CodeBuilder} from "./codegen/CodeBuilder"
import {Rpc4TsClientGenerationOptions} from "./ClientGenerator"
import {ApiDefinition, RpcModelKind, RpcType, RpcTypeNames} from "rpc4ts-runtime";
import {isBuiltinType, modelType, typescriptRpcType} from "./Rpc4tsType"
import {buildSerializer} from "./SerializerGenerator"
import {TsTypes, TsNamespace, MaybeFormattedString, concat, join} from "./codegen/FormatString"


 const RPC_CLIENT = TsTypes.library("RpcClient", "RpcClient")
 const SERIALIZATION_FORMAT = TsTypes.library("SerializationFormat", "SerializationFormat")

/**
 * @param api definition with default values
 * @param rawApi definition without default values
 */
export function generateAccessor(api: ApiDefinition, rawApi: ApiDefinition, options: Rpc4TsClientGenerationOptions): string {
    const builder = new CodeBuilder(options.localLibPaths)


    // const referencedModels = api.models.filter(model => model.type !== RpcModelKind.inline)
    //     .map(model => modelType(model.name))

    // builder.addImport(["RpcClient"], libraryPath("RpcClient", options))
    //     .addImport(["SerializationFormat"], libraryPath("SerializationFormat", options))
    //     .addImport(["GeneratedCodeUtils"], libraryPath("impl/GeneratedCodeUtils", options))
    //     // .addImport(["Rpc4aTypeAdapter"], libraryPath("impl/Rpc4aTypeAdapter", options))
    //     .addImport(getReferencedGeneratedTypeNames(api), `./rpc4ts_${api.name}Models`)
    //     // .addImport(["UserProtocolRuntimeModels"], `./${api.name}RuntimeModels`)
    //     .addImport(["Dayjs"], `dayjs`)
    //     .addImport(["Duration"], `dayjs/plugin/duration`)
    //     .addImport(referencedModels.map(model => serializerName(model)), `./rpc4ts_${api.name}Serializers`)

    // addSerializerImports(builder, options)

    const runtimeModelsName = `${api.name}RuntimeModels`

    // builder._addLineOfCode(`const ${runtimeModelsName} = \`${JSON.stringify(rawApi.models)}\``)

    builder.addClass({name: `${api.name}Api`}, (clazz) => {
        clazz.addProperty({name: "private readonly client", type: RPC_CLIENT})
            .addProperty({name: "private readonly format", type: SERIALIZATION_FORMAT})
            // .addProperty({
            //     name: "private readonly adapter",
            //     type: "Rpc4aTypeAdapter",
            //     initializer: `GeneratedCodeUtils.createTypeAdapter(${runtimeModelsName})`
            // })
            .addConstructor([["client", RPC_CLIENT], ["format", SERIALIZATION_FORMAT]], constructor => {
                constructor.addAssignment("this.client", "client")
                    .addAssignment("this.format", "format")
            })

        for (const method of api.methods) {
            const nonPromiseReturnType = typescriptRpcType(method.returnType, api.name)
            // We wrap the return type with a promise because api methods are network calls
            const returnType = TsTypes.promise(nonPromiseReturnType)
            // const returnType: RpcType = {
            //     name: "Promise",
            //     isNullable: false,
            //     typeArguments: [method.returnType],
            //     isTypeParameter: false,
            //     inlinedType: undefined
            // }
            // const returnTypeString = typescriptRpcType(returnType)
            clazz.addFunction(
                method.name,
                method.parameters.map(param => [param.name, typescriptRpcType(param.type, api.name)]),
                returnType,
                (body) => {
                    const args = [
                        "this.client", "this.format", `"${method.name}"`,
                        arrayLiteral(method.parameters.map(param => param.name)),
                        // Get rid of default values to be the type that is manually inputted be shorter
                        arrayLiteral(method.parameters.map(param => buildSerializer(param.type, {}, api.name)))
                    ]
                    const isVoid = method.returnType.name === RpcTypeNames.Void
                    if (!isVoid) {
                        const retvalSerializer = buildSerializer(method.returnType, {}, api.name)
                        // Add return type if it's not null
                        args.push(retvalSerializer)
                    }
                    // const functionName = isVoid ? "send" : "request"
                    body.addReturningFunctionCall(
                        isVoid? GENERATED_CODE_UTILS_SEND: GENERATED_CODE_UTILS_REQUEST,
                        // Add all the parameters as a trailing argument to GeneratedCodeUtils.request
                        args,
                        false
                    )
                    if (method.returnType.name === RpcTypeNames.Tuple) {
                        // The type for tuples is not recognized correctly so we need to explicitly cast it
                        body.addCast(returnType)
                    }
                    body.addEmptyLine()
                })
        }
    })

    return builder.build()
}

const GENERATED_CODE_UTILS = TsNamespace.library("GeneratedCodeUtils", "impl/GeneratedCodeUtils")

const GENERATED_CODE_UTILS_SEND = GENERATED_CODE_UTILS.function("send")
const GENERATED_CODE_UTILS_REQUEST = GENERATED_CODE_UTILS.function("request")

// _SEND = TsFunction.library("send",
//     "impl/GeneratedCodeUtils",
//     {namespace: "GeneratedCodeUtils"}
// )

// function stringifyToJsObject(obj: unknown): string {
//     return JSON.stringify(obj).replace(/"(\w+)"\s*:/g, '$1:')
// }

function arrayLiteral(list: MaybeFormattedString[]): MaybeFormattedString {
    return concat("[" ,join(list, ", "),  "]")
}

// /**
//  * Gets all the custom generated classes that the api client accessor referenced so we can import them
//  */
// function getReferencedGeneratedTypeNames(api: ApiDefinition): string[] {
//     const names: Set<string> = new Set()
//     for (const method of api.methods) {
//         for (const param of method.parameters) {
//             addReferencedGeneratedTypeNames(param.type, names)
//         }
//         addReferencedGeneratedTypeNames(method.returnType, names)
//     }
//     return Array.from(names)
// }

// function addReferencedGeneratedTypeNames(type: RpcType, addTo: Set<string>) {
//     // if (type.inlinedType !== undefined) {
//     //     addReferencedGeneratedTypeNames(type.inlinedType, addTo)
//     // } else {
//     if (!isBuiltinType(type)) {
//         addTo.add(modelName(type.name))
//     }
//     for (const typeArgument of type.typeArguments) {
//         addReferencedGeneratedTypeNames(typeArgument, addTo)
//     }
//     // }
// }
