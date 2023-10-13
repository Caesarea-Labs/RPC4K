export interface ApiDefinition {
    name: RpcClassSurrogate;
    methods: RpcDefinition[];
    models: RpcModel[];
}

export interface RpcClassSurrogate {
    name: string;
    isOptional: boolean;
    typeArguments: RpcClassSurrogate[];
}

export interface RpcDefinition {
    name: string;
    parameters: RpcParameter[];
    returnType: RpcClassSurrogate;
}

export type RpcModel = RpcModel.Enum | RpcModel.Struct | RpcModel.Union

export namespace RpcModel {
    export enum Type {
        enum = "enum",
        struct = "struct",
        union = "union",
    }

    export interface Enum {
        type: RpcModel.Type.enum;
        name: string;
        options: string[];
    }

    export interface Struct {
        type: RpcModel.Type.struct;
        name: string;
        typeParameters: string[];
        properties: { [key: string]: RpcType };
    }

    export interface Union {
        type: RpcModel.Type.union;
        name: string;
        options: string[];
    }
}

export interface RpcParameter {
    name: string;
    type: RpcClassSurrogate;
}

export interface RpcType {
    name: string;
    isTypeParameter: boolean;
    isOptional: boolean;
    typeArguments: RpcType[];
}

