import {
    ApiDefinition,
    createRpcType,
    PartialRpcType,
    RpcDefinition,
    RpcModel,
    RpcModelKind,
    RpcType
} from "../ApiDefinition";

export function fillDefaultApiDefinitionValues(partialDefinition: ApiDefinition): ApiDefinition {
    // No simpler way to do this other than iterate through the entire object i think
    return {
        name: partialDefinition.name,
        models: partialDefinition.models.map(model => fillDefaultModelValues(model)),
        methods: partialDefinition.methods.map(method => fillDefaultMethodValues(method))
    }
}

export function fillDefaultModelValues(partialModel: RpcModel): RpcModel {
    switch (partialModel.type) {
        case RpcModelKind.enum:
            return partialModel
        case RpcModelKind.struct: {
            const {properties, typeParameters, ...other} = partialModel
            return {
                properties: properties.map(({name, type, isOptional}) => {
                    return ({name, type: createRpcType(type), isOptional: isOptional ?? false})
                }),
                // Default for typeParameters is []
                typeParameters: typeParameters ?? [],
                ...other
            }
        }
        case RpcModelKind.union: {
            const {options, typeParameters, ...other} = partialModel
            return {
                options: options.map(value => createRpcType(value)),
                // Default for typeParameters is []
                typeParameters: typeParameters ?? [],
                ...other
            }
        }
    }
}


export function fillDefaultMethodValues(partialMethod: RpcDefinition): RpcDefinition {
    return {
        name: partialMethod.name,
        parameters: partialMethod.parameters.map(param => ({name: param.name, type: createRpcType(param.type)})),
        returnType: createRpcType(partialMethod.returnType)
    }
}


export function stripDefaultTypeValues(type: RpcType): PartialRpcType {
    if (!type.isTypeParameter && !type.isNullable && type.inlinedType === undefined) {
        if (type.typeArguments.length === 0) {
            // If everything is default except for the name, we can just use a simple string instead
            return type.name
        } else {
            // If everything is default except for the name and type arguments, we can just use an array.
            return [type.name, type.typeArguments.map(arg => stripDefaultTypeValues(arg))]
        }
    }
    return {
        name: type.name,
        typeArguments: type.typeArguments.length === 0 ? undefined : type.typeArguments.map(arg => stripDefaultTypeValues(arg)),
        inlinedType: type.inlinedType === undefined ? undefined : stripDefaultTypeValues(type.inlinedType),
        isTypeParameter: !type.isTypeParameter ? undefined : type.isTypeParameter,
        isNullable: !type.isNullable ? undefined : type.isNullable
    }
}