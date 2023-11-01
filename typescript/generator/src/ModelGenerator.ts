import {CodeBuilder} from "./CodeBuilder";
import {
    RpcEnumModel,
    RpcInlineModel,
    RpcModel,
    RpcModelKind,
    RpcStructModel,
    RpcTypeDiscriminator,
    RpcUnionModel,
    simpleModelName,
} from "rpc4ts-runtime";
import {modelName, typescriptRpcType} from "./Rpc4tsType";

export function generateModels(models: RpcModel[]): string {
    const builder = new CodeBuilder()
        .addImport(["Dayjs"], `dayjs`)
        .addImport(["Duration"], `dayjs/plugin/duration`)

    for (const model of models) {
        switch (model.type) {
            case RpcModelKind.struct:
                addStruct(builder, model)
                break;
            case RpcModelKind.enum:
                addEnum(builder, model)
                break;
            case RpcModelKind.union:
                addUnion(builder, model)
                break;
            case RpcModelKind.inline:
                addInlineType(builder, model)

        }
    }
    return builder.build()
}

function addStruct(code: CodeBuilder, struct: RpcStructModel) {
    const name = modelName(struct.name)
    code.addInterface({name, typeParameters: struct.typeParameters}, interfaceBuilder => {
        struct.properties.forEach(({name, type, isOptional}) => {
            interfaceBuilder.addProperty(
                {
                    // "type" is a reserved, and we use the simple model name to make the code more ergonomic
                    name, optional: isOptional, type: typescriptRpcType(type)
                }
            )
        })

        // Add type discriminator if needed with a string type that can only be a specific value that is the model name
        if (struct.hasTypeDiscriminator) {
            interfaceBuilder.addProperty({name: RpcTypeDiscriminator, optional: false, type: `"${simpleModelName(struct.name)}"`})
        }
    })
}

function addEnum(code: CodeBuilder, enumModel: RpcEnumModel) {
    code.addUnionType({name: modelName(enumModel.name), types: enumModel.options.map(option => `"${modelName(option)}"`)})
}

function addUnion(code: CodeBuilder, struct: RpcUnionModel) {
    code.addUnionType({
        name: modelName(struct.name),
        types: struct.options.map(option => typescriptRpcType(option)),
        typeParameters: struct.typeParameters
    })
}


function addInlineType(code: CodeBuilder, model: RpcInlineModel) {
    code.addTypeAlias({
        name: modelName(model.name),
        type: typescriptRpcType(model.inlinedType),
        typeParameters: model.typeParameters
    })
}

