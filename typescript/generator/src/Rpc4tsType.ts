
import {RpcType, RpcTypeNames} from "rpc4ts-runtime";


export function typescriptRpcType(type: RpcType): string {
    // if (type.inlinedType !== undefined) return typescriptRpcType(type.inlinedType)
    // Handle | null adding
    const nullableString = type.isNullable ? " | null" : ""
    // Handle other things
    return typescriptRpcTypeIgnoreOptional(type) + nullableString
}

function typescriptRpcTypeIgnoreOptional(type: RpcType): string {
    // If it's a type parameter we don't care if it's a builtin type, we treat it as a type parameter.
    if (type.isTypeParameter) return type.name
    const builtinType = resolveBuiltinType(type)
    if (builtinType !== undefined) return builtinType
    const typeArgumentString = type.typeArguments.length === 0 ? ""
        : `<${type.typeArguments.map(arg => typescriptRpcType(arg)).join(", ")}>`

    return modelName(type.name) + typeArgumentString
}

export function isBuiltinType(type: RpcType): boolean {
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
        case "uuid":
            return "string"
        case "duration":
            // Durations are Dayjs.Duration in typescript
            return "Duration"
        case  RpcTypeNames.Time:
            // Dates are Dayjs in typescript
            return "Dayjs"
        case RpcTypeNames.Arr: {
            const typeArgs = type.typeArguments
            if (typeArgs.length !== 1) {
                throw new Error(`Array type had an unexpected amount of type arguments: ${typeArgs.length}`)
            }

            const elementType = typeArgs[0]
            const elementTypeString = typescriptRpcType(elementType)
            // Add brackets in case the element type is nullable to avoid ambiguity
            const elementTypeStringWithBrackets = elementType.isNullable ? `(${elementTypeString})` : elementTypeString
            // Typescript arrays are T[]
            return `${elementTypeStringWithBrackets}[]`
        }
        case RpcTypeNames.Rec : {
            const typeArgs = type.typeArguments
            if (typeArgs.length !== 2) {
                throw new Error(`Record type had an unexpected amount of type arguments: ${typeArgs.length}`)
            }
            const keyType = typescriptRpcType(typeArgs[0])
            const underlyingKeyType = typescriptRpcType(resolveToUnderlying(typeArgs[0]))
            if (underlyingKeyType !== "string" && underlyingKeyType !== "number") {
                // NiceToHave: Support complex keys in Typescript
                throw new Error(`Unsupported map key type: ${keyType} in type: ${JSON.stringify(type)}`)
            }
            const valueType = typescriptRpcType(typeArgs[1])
            // Typescript Records are Record<K,V>
            return `Record<${keyType}, ${valueType}>`
        }
        case RpcTypeNames.Tuple: {
            // Typescript tuples are [T1, T2, ..., Tn]
            return `[${type.typeArguments.map(arg => typescriptRpcType(arg)).join(", ")}]`
        }
        case RpcTypeNames.Void:
            return "void"
        default:
            return undefined
    }
}

function resolveToUnderlying(type: RpcType): RpcType {
    if (type.inlinedType !== undefined) {
        return resolveToUnderlying(type.inlinedType)
    } else {
        return type
    }
}

/**
 * Converts the Rpc representation of a struct name to the typescript representation
 */
export function modelName(name: string): string {
    // Treat "Foo.Bar" as "FooBar"
    return name.replace(/\./g, "")
}