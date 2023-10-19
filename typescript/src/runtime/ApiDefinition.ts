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

export interface RpcTypeName {
    className: string
}

export interface RpcType {
    name: string;
    packageName?: string;
    isTypeParameter: boolean;
    isNullable: boolean;
    typeArguments: RpcType[];
    inlinedType: RpcType | undefined
}

//TODO: once we have gotten rid of the packageName shit, we can restore saving PartialRpcType as a simple string or tuple.
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
    //TODO: restore this without packageName
    // if (!type.isTypeParameter && !type.isNullable && type.inlinedType === undefined) {
    //     if (type.typeArguments.length === 0) {
    //         // If everything is default except for the name, we can just use a simple string instead
    //         return type.name
    //     } else {
    //         // If everything is default except for the name and type arguments, we can just use an array.
    //         return [type.name, type.typeArguments.map(arg => stripDefaultTypeValues(arg))]
    //     }
    // }
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

//TODO: try to see if we can change this to _type to not step over people's toes
// A problem I see right now is that js users will have to with an ugly "_type" property.
// So if I can hide this handling, maybe with classes, I could change what the discriminator is.
export const RpcTypeDiscriminator = "type"

export namespace RpcTypes {
    export const Void: RpcType = createRpcType({name: RpcTypeNames.Void})
}

