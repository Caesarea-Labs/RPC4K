import {RpcModel, RpcType} from "./ApiDefinition";
import {CodeBuilder} from "./CodeBuilder";
import {recordForEach} from "./Utils"

export function generateModels(models: RpcModel[]): string {
    const builder = new CodeBuilder()
    for (const model of models) {
        switch (model.type) {
            case RpcModel.Type.struct:
                addStruct(builder, model)
                break;
            case RpcModel.Type.enum:
                addEnum(builder, model)
                break;
            case RpcModel.Type.union:
                addUnion(builder, model)
                break;

        }
    }
    return builder.build()
}

function addStruct(code: CodeBuilder, struct: RpcModel.Struct) {
    code.addInterface({name: struct.name, typeParameters: struct.typeParameters}, interfaceBuilder => {
        recordForEach(struct.properties, (name, type) => {
            interfaceBuilder.addProperty({name, optional: type.isOptional, type: typescriptRpcTypeIgnoreOptional(type)})
        })
    })
}

function addEnum(code: CodeBuilder, enumModel: RpcModel.Enum) {
    code.addUnionType({name: enumModel.name, types: enumModel.options.map(option => `"${option}"`)})
}

function addUnion(code: CodeBuilder, struct: RpcModel.Union) {
    code.addUnionType({
        name: struct.name,
        types: struct.options.map(option => typescriptRpcType(option)),
        typeParameters: struct.typeParameters
    })
}

/**
 * @param interpretOptionalAsUndefinedUnion Normally, an optional type in Typescript is `T | undefined`. However,
 * in property declarations the `| undefined` can be omitted if the property is denoted as optional with `?`.
 */
function typescriptRpcType(type: RpcType): string {
    const undefinedString = type.isOptional ? " | undefined" : ""
    return typescriptRpcTypeIgnoreOptional(type) + undefinedString
}

/**
 * Normally, an optional type in Typescript is `T | undefined`. However,
 * in property and parameter declarations the `| undefined` can be omitted if the property is denoted as optional with `?`.
 */
function typescriptRpcTypeIgnoreOptional(type: RpcType): string {
    // If it's a type parameter we don't care if it's a builtin type, we treat it as a type parameter.
    if (type.isTypeParameter) return type.name
    const builtinType = resolveBuiltinType(type)
    if (builtinType !== undefined) return builtinType
    const typeArgumentString = type.typeArguments.length === 0 ? ""
        : `<${type.typeArguments.map(arg => typescriptRpcType(arg)).join(", ")}>`
    return type.name + typeArgumentString
}

function resolveBuiltinType(type: RpcType): string | undefined {
    switch (type.name) {
        case "bool" :
            return "boolean"
        case "i8":
        case "i16":
        case "i32":
        case "i64":
        case "f32":
        case "f64":
            return "number"
        case "char":
        case "string":
            return "string"
        case "array": {
            const typeArgs = type.typeArguments
            if (typeArgs.length !== 1) {
                throw new Error(`Array type had an unexpected amount of type arguments: ${typeArgs.length}`)
            }
            // Typescript arrays are T[]
            return `${typescriptRpcType(typeArgs[0])}[]`
        }
        case "record" : {
            const typeArgs = type.typeArguments
            if (typeArgs.length !== 2) {
                throw new Error(`Record type had an unexpected amount of type arguments: ${typeArgs.length}`)
            }
            const keyType = typescriptRpcType(typeArgs[0])
            if (keyType !== "string" && keyType !== "number") {
                //TODO: support it
                throw new Error(`Unsupported map key type: ${keyType} in type: ${JSON.stringify(type)}`)
            }
            const valueType = typescriptRpcType(typeArgs[1])
            // Typescript Records are Record<K,V>
            return `Record<${keyType}, ${valueType}>`
        }
        case "tuple": {
            // Typescript tuples are [T1, T2, ..., Tn]
            return `[${type.typeArguments.map(arg => typescriptRpcType(arg)).join(", ")}]`
        }
        case "void":
            return "void"
        default:
            return undefined
    }
}


// @Serializable
// data class EveryBuiltinType(
//     val n: Map<Int,Int>,
//     val o: Set<Int>,
//     val p: Pair<Int,Int>,
//     val q: Triple<Int,Int,Int>,
//     val r: Unit
// ) {

////interface Dog {
// //     name: string
// //     type: string
// //     age: number
// // }