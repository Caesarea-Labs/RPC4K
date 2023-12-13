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
        .ignoreAnyWarnings()
    builder.addClass({name: `${api.name}Api`}, (clazz) => {
        clazz.addProperty({name: "private readonly client", type: RPC_CLIENT})
            .addProperty({name: "private readonly format", type: SERIALIZATION_FORMAT})
            .addConstructor([["client", RPC_CLIENT], ["format", SERIALIZATION_FORMAT]], constructor => {
                constructor.addAssignment("this.client", "client")
                    .addAssignment("this.format", "format")
            })

        for (const method of api.methods) {
            const nonPromiseReturnType = typescriptRpcType(method.returnType, api.name)
            // We wrap the return type with a promise because api methods are network calls
            const returnType = TsTypes.promise(nonPromiseReturnType)

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


function arrayLiteral(list: MaybeFormattedString[]): MaybeFormattedString {
    return concat("[" ,join(list, ", "),  "]")
}
