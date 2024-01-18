export interface ApiDefinition {
    name: string;
    methods: RpcDefinition[];
    models: RpcModel[];
    events: RpcEventEndpoint[]
}

export interface RpcDefinition {
    name: string;
    parameters: RpcParameter[];
    returnType: RpcType;
}

export interface RpcEventEndpoint {
    name: string
    parameters: EventParameter[]
    returnType: RpcType
}

export interface EventParameter {
    isDispatch: boolean
    isTarget: boolean
    value: RpcParameter
}


export type RpcModel = RpcEnumModel | RpcStructModel | RpcUnionModel | RpcInlineModel

export enum RpcModelKind {
    enum = "enum",
    struct = "struct",
    union = "union",
    inline = "inline"
}

export interface RpcEnumModel {
    type: RpcModelKind.enum;
    name: string;
    options: string[];
}

export interface RpcStructModel {
    type: RpcModelKind.struct;
    name: string;
    // BLOCKED: get rid of this when we can coerce kotlinx.serialization to use simple names
    packageName: string;
    hasTypeDiscriminator: boolean
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
export interface RpcInlineModel {
    type: RpcModelKind.inline;
    name: string;
    typeParameters: string[]
    inlinedType: RpcType
}


export interface RpcParameter {
    name: string;
    type: RpcType;
}


export interface RpcType {
    name: string;
    // packageName?: string;
    isTypeParameter: boolean;
    isNullable: boolean;
    typeArguments: RpcType[];
    inlinedType: RpcType | undefined
}


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


