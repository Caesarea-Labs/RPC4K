import {PartialRpcType, PartialRpcTypeObject, RpcType} from "./ApiDefinition";

export function createRpcType(partialType: PartialRpcType): RpcType {
    return createRpcTypeFromObject(partialType)
}

function createRpcTypeFromObject(partialType: PartialRpcTypeObject): RpcType {
    return {
        // Default values is []
        typeArguments: partialType.typeArguments?.map(arg => createRpcType(arg)) ?? [],
        inlinedType: partialType.inlinedType === undefined ? undefined : createRpcType(partialType.inlinedType),
        // Default values are false
        isTypeParameter: partialType.isTypeParameter ?? false,
        isNullable: partialType.isNullable ?? false,
        name: partialType.name
    }
}

export function stripDefaultTypeValues(type: RpcType): PartialRpcType {
    // SLOW: Compact RpcType generation in Typescript
    return {
        name: type.name,
        typeArguments: type.typeArguments.length === 0 ? undefined : type.typeArguments.map(arg => stripDefaultTypeValues(arg)),
        inlinedType: type.inlinedType === undefined ? undefined : stripDefaultTypeValues(type.inlinedType),
        isTypeParameter: !type.isTypeParameter ? undefined : type.isTypeParameter,
        isNullable: !type.isNullable ? undefined : type.isNullable
    }
}

export namespace RpcTypeNames {
    export const Tuple = "tuple"
    export const Arr = "array"
    export const Dur = "duration"
    export const Rec = "record"
    export const Void = "void"
    export const Time = "date"
    export const Str = "string"
}
export const RpcTypeDiscriminator = "type"
export namespace RpcTypes {
    export const Void: RpcType = createRpcType({name: RpcTypeNames.Void})
    export const Str: RpcType = createRpcType({name: RpcTypeNames.Str})
}