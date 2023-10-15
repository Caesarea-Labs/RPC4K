export interface ApiDefinition {
    name: RpcType;
    methods: RpcDefinition[];
    models: RpcModel[];
}

export interface RpcDefinition {
    name: string;
    parameters: RpcParameter[];
    returnType: RpcType;
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
        properties: Record<string, RpcType>;
    }

    export interface Union {
        type: RpcModel.Type.union;
        name: string;
        typeParameters: string[]
        options: RpcType[];
    }
}

export interface RpcParameter {
    name: string;
    type: RpcType;
}

export interface RpcType {
    name: string;
    isTypeParameter: boolean;
    isOptional: boolean;
    typeArguments: RpcType[];
}
