import {RpcType, RpcTypeNames} from "rpc4ts-runtime";
import {tsReferenceToString, TsType, TsTypes} from "./codegen/FormatString";


export function typescriptRpcType(type: RpcType): TsType {
    // if (type.inlinedType !== undefined) return typescriptRpcType(type.inlinedType)
    // Handle | null adding
    const withoutNull = typescriptRpcTypeIgnoreOptional(type)
    if (type.isNullable) {
        return TsTypes.nullable(withoutNull)
    } else {
        return withoutNull
    }

    // const nullableString = type.isNullable ? " | null" : ""
    // // Handle other things
    // return typescriptRpcTypeIgnoreOptional(type) + nullableString
}

// We keep this constant for now to make things simpler
const ModelFile = "./rpc4ts_AllEncompassingServiceModels"

function typescriptRpcTypeIgnoreOptional(type: RpcType): TsType {
    // If it's a type parameter we don't care if it's a builtin type, we treat it as a type parameter.
    if (type.isTypeParameter) return TsTypes.typeParameter(type.name)
    const builtinType = resolveBuiltinType(type)
    if (builtinType !== undefined) return builtinType
    const typeArguments = type.typeArguments.map(arg => typescriptRpcType(arg))
    // const typeArgumentString = type.typeArguments.length === 0 ? ""
    //     : `<${).join(", ")}>`

    return TsTypes.create(modelName(type.name), ModelFile, typeArguments)

    // return modelName(type.name) + typeArgumentString
}

export function isBuiltinType(type: RpcType): boolean {
    return resolveBuiltinType(type) !== undefined
}

function resolveBuiltinType(type: RpcType): TsType | undefined {
    switch (type.name) {
        case "bool" :
            return TsTypes.BOOLEAN
        case "i8":
        case "i16":
        case "i32":
        case "i64":
        case "f32":
        case "f64":
            return TsTypes.NUMBER
        case "char":
        case "string":
        case "uuid":
            return TsTypes.STRING
        case "duration":
            // Durations are Dayjs.Duration in typescript
            return TsTypes.DURATION
        case  RpcTypeNames.Time:
            // Dates are Dayjs in typescript
            return TsTypes.DAYJS
        case RpcTypeNames.Arr: {
            const typeArgs = type.typeArguments
            if (typeArgs.length !== 1) {
                throw new Error(`Array type had an unexpected amount of type arguments: ${typeArgs.length}`)
            }

            const elementType = typeArgs[0]
            const elementTypeReference = typescriptRpcType(elementType)

            return TsTypes.array(elementTypeReference)


            // // Add brackets in case the element type is nullable to avoid ambiguity
            // const elementTypeStringWithBrackets = elementType.isNullable ? `(${elementTypeReference})` : elementTypeReference
            // // Typescript arrays are T[]
            // return `${elementTypeStringWithBrackets}[]`
        }
        case RpcTypeNames.Rec : {
            const typeArgs = type.typeArguments
            if (typeArgs.length !== 2) {
                throw new Error(`Record type had an unexpected amount of type arguments: ${typeArgs.length}`)
            }
            const keyType = typescriptRpcType(typeArgs[0])
            const underlyingKeyType = typescriptRpcType(resolveToUnderlying(typeArgs[0]))
            if (underlyingKeyType !== TsTypes.STRING && underlyingKeyType !== TsTypes.NUMBER) {
                // NiceToHave: Support complex keys in Typescript
                throw new Error(`Unsupported map key type: ${tsReferenceToString(keyType)} in type: ${JSON.stringify(type)}`)
            }
            const valueType = typescriptRpcType(typeArgs[1])
            return TsTypes.record(keyType,valueType)
            // Typescript Records are Record<K,V>
            // return `Record<${keyType}, ${valueType}>`
        }
        case RpcTypeNames.Tuple: {
            return TsTypes.tuple(type.typeArguments.map(arg => typescriptRpcType(arg)))
            // Typescript tuples are [T1, T2, ..., Tn]
            // return `[${.join(", ")}]`
        }
        case RpcTypeNames.Void:
            return TsTypes.VOID
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