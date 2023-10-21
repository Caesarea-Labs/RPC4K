import {PartiallyPartial} from "./impl/Util";

export interface ApiDefinition {
    name: string;
    methods: RpcDefinition[];
    models: RpcModel[];
}

export interface RpcDefinition {
    name: string;
    parameters: RpcParameter[];
    returnType: RpcType;
}

export type RpcModel = RpcEnumModel | RpcStructModel | RpcUnionModel

export enum RpcModelKind {
    enum = "enum",
    struct = "struct",
    union = "union",
}

export interface RpcEnumModel {
    type: RpcModelKind.enum;
    name: string;
    options: string[];
}

export interface RpcStructModel {
    type: RpcModelKind.struct;
    name: string;
    typeParameters: string[];
    properties: RpcStructProperty[];
}

export interface RpcStructProperty {
    name: string
    type: RpcType
    isOptional: boolean
}

export interface RpcUnionModel {
    type: RpcModelKind.union;
    name: string;
    typeParameters: string[]
    options: RpcType[];
}


export interface RpcParameter {
    name: string;
    type: RpcType;
}


export interface RpcType {
    name: string;
    packageName?: string;
    isTypeParameter: boolean;
    isNullable: boolean;
    typeArguments: RpcType[];
    inlinedType: RpcType | undefined
}

const x: [number, number?] = [1]

//SLOW: Compact RpcType generation in Typescript
/**
 * A simple string may be specified to denote a type with only a name
 * An array with a string as the first element and types as the other elements may be specified to denote a type with only a name and type arguments
 */
export type PartialRpcType = /*string | [string, PartialRpcType[]] |*/ PartialRpcTypeObject


export interface PartialRpcTypeObject {
    name: string
    packageName?: string
    isTypeParameter?: boolean
    isNullable?: boolean
    typeArguments?: PartialRpcType[],
    inlinedType?: PartialRpcType
}

export function createRpcType(partialType: PartialRpcType): RpcType {
    return createRpcTypeFromObject(partialType)
    // if (typeof partialType === "string") {
    //     return createRpcTypeFromObject({name: partialType})
    // } else {
    //     if (Array.isArray(partialType)) {
    //         const [name, typeArguments] = partialType
    //         return createRpcTypeFromObject({name, typeArguments})
    //     } else {
    //         return createRpcTypeFromObject(partialType)
    //     }
    // }
}

function createRpcTypeFromObject(partialType: PartialRpcTypeObject): RpcType {
    return {
        // Default values is []
        typeArguments: partialType.typeArguments?.map(arg => createRpcType(arg)) ?? [],
        inlinedType: partialType.inlinedType === undefined ? undefined : createRpcType(partialType.inlinedType),
        // Default values are false
        isTypeParameter: partialType.isTypeParameter ?? false,
        isNullable: partialType.isNullable ?? false,
        name: partialType.name,
        packageName: partialType.packageName
    }
}

export function stripDefaultTypeValues(type: RpcType): PartialRpcType {
    // SLOW: Compact RpcType generation in Typescript
    return {
        name: type.name,
        typeArguments: type.typeArguments.length === 0 ? undefined : type.typeArguments.map(arg => stripDefaultTypeValues(arg)),
        inlinedType: type.inlinedType === undefined ? undefined : stripDefaultTypeValues(type.inlinedType),
        isTypeParameter: !type.isTypeParameter ? undefined : type.isTypeParameter,
        isNullable: !type.isNullable ? undefined : type.isNullable,
        packageName: type.packageName
    }
}


export namespace RpcTypeNames {
    export const Tuple = "tuple"
    export const Arr = "array"
    export const Rec = "record"
    export const Void = "void"
}


export const RpcTypeDiscriminator = "type"

export namespace RpcTypes {
    export const Void: RpcType = createRpcType({name: RpcTypeNames.Void})
}

