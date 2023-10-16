import {RpcType} from "./ApiDefinition";


export function typescriptRpcType(type: RpcType): string {
    if (type.inlinedType !== null) return typescriptRpcType(type.inlinedType)
    const undefinedString = type.isOptional ? " | undefined" : ""
    return typescriptRpcTypeIgnoreOptional(type) + undefinedString
}


/**
 * Normally, an optional type in Typescript is `T | undefined`. However,
 * in property and parameter declarations the `| undefined` can be omitted if the property is denoted as optional with `?`.
 */
export function typescriptRpcTypeIgnoreOptional(type: RpcType): string {
    // If it's a type parameter we don't care if it's a builtin type, we treat it as a type parameter.
    if (type.isTypeParameter) return type.name
    const builtinType = resolveBuiltinType(type)
    if (builtinType !== undefined) return builtinType
    const typeArgumentString = type.typeArguments.length === 0 ? ""
        : `<${type.typeArguments.map(arg => typescriptRpcType(arg)).join(", ")}>`
    return type.name + typeArgumentString
}

export function isBuiltinType(type: RpcType) : boolean {
    return resolveBuiltinType(type) !== undefined
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