import {ClassBuilder, CodeBuilder} from "./codegen/CodeBuilder"
import {Rpc4TsClientGenerationOptions} from "./ClientGenerator"
import {typescriptRpcType} from "./Rpc4tsType"
import {buildSerializer} from "./SerializerGenerator"
import {concat, join, MaybeFormattedString, TsNamespace, TsType, TsTypes} from "./codegen/FormatString"
import {ApiDefinition, RpcDefinition, RpcEventEndpoint} from "./ApiDefinition";
import {RpcTypeNames} from "./RpcTypeUtils";


const RPC_CLIENT = TsTypes.library("RpcClient", "RpcClient")
const SERIALIZATION_FORMAT = TsTypes.library("SerializationFormat", "SerializationFormat")

function RESPONSE(responseType: TsType): TsType {
    return TsTypes.library("Response", "Response", responseType)
}

function OBSERVABLE(eventType: TsType): TsType {
    return TsTypes.library("Observable", "Observable", eventType)
}


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
            addRpcAccessor(method, api, clazz);
        }

        for (const event of api.events) {
            addEventSubscriber(event, api, clazz);
        }
    })

    return builder.build()
}

function addEventSubscriber(event: RpcEventEndpoint, api: ApiDefinition, clazz: ClassBuilder) {
    const subscriptionParams = event.parameters.filter(param => !param.isDispatch)
    const params: [string, TsType][] = subscriptionParams
        .map(param => [param.value.name, typescriptRpcType(param.value.type, api.name)])

    const returnType = OBSERVABLE(typescriptRpcType(event.returnType, api.name))

    clazz.addFunction(event.name, params, returnType, (body) => {
        const args = [
            "this.client", "this.format", `"${event.name}"`,
            arrayLiteral(params.map(([name]) => name)),
            // Param serializers
            arrayLiteral(subscriptionParams.map((param) => buildSerializer(param.value.type, {}, api.name))),
            // Return value serializers
            buildSerializer(event.returnType, {}, api.name)
        ]
        body.addReturningFunctionCall(GENERATED_CODE_UTILS_CREATE_OBSERVABLE, args)
    })
}

//     function subscribeToEventTest(param: string): Observable<string> {
//         return GeneratedCodeUtils.createObservable(fetch, format, "eventTest", [param], [StringSerializer],
//             StringSerializer)
//     }
function addRpcAccessor(method: RpcDefinition, api: ApiDefinition, clazz: ClassBuilder) {
    const nonPromiseReturnType = typescriptRpcType(method.returnType, api.name)
    // We wrap the return type with a promise because api methods are network calls
    const returnType = RESPONSE(nonPromiseReturnType)

    clazz.addFunction(
        method.name,
        method.parameters.map(param => [param.name, typescriptRpcType(param.type, api.name)]),
        returnType,
        (body) => {
            const args = [
                "this.client", "this.format", `"${method.name}"`,
                arrayLiteral(method.parameters.map(param => param.name)),
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
                isVoid ? GENERATED_CODE_UTILS_SEND : GENERATED_CODE_UTILS_REQUEST,
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

const GENERATED_CODE_UTILS = TsNamespace.library("GeneratedCodeUtils", "impl/GeneratedCodeUtils")

const GENERATED_CODE_UTILS_SEND = GENERATED_CODE_UTILS.function("send")
const GENERATED_CODE_UTILS_REQUEST = GENERATED_CODE_UTILS.function("request")
const GENERATED_CODE_UTILS_CREATE_OBSERVABLE = GENERATED_CODE_UTILS.function("createObservable")


function arrayLiteral(list: MaybeFormattedString[]): MaybeFormattedString {
    return concat("[", join(list, ", "), "]")
}
