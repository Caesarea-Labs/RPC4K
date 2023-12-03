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

/**
 * Classes are used over interfaces because they allow greater control over what is constructed and exposed.
 * In optional properties, we can allow clients to construct them without specifying the property value, but still expose the
 * property as a non-nullable value assuming the server always gives it a value.
 */
function addStruct(code: CodeBuilder, struct: RpcStructModel) {
    const name = modelName(struct.name)
    code.addClass({name, typeParameters: struct.typeParameters}, classBuilder => {
        struct.properties.forEach(({name, type, isOptional}) => {
            classBuilder.addProperty(
                //TODO: handle isOptional properly
                {
                    name: `readonly ${name}`, optional: isOptional, type: typescriptRpcType(type)
                }
            )
        })

        // Add type discriminator if needed with a string type that can only be a specific value that is the model name
        // if (struct.hasTypeDiscriminator) {
        //     classBuilder.addProperty({name: RpcTypeDiscriminator, optional: false, type: `"${simpleModelName(struct.name)}"`})
        // }

        const propertyNames = struct.properties.map(property => property.name).join(", ")
        const propertyTypes = struct.properties.map(property => `${property.name}: ${typescriptRpcType(property.type)}`).join(", ")
        // Only one parameter: the object argument
        classBuilder.addConstructor([[`{${propertyNames}}`,`{${propertyTypes}}`]], ctrBuilder => {
            struct.properties.forEach(({name, type, isOptional}) => {
                //TODO: handle isOptional

                ctrBuilder.addAssignment(`this.${name}`,`${name}`)

                // classBuilder.addProperty(
                //     {
                //         // "type" is a reserved, and we use the simple model name to make the code more ergonomic
                //         name: `readonly ${name}`, optional: isOptional, type: typescriptRpcType(type)
                //     }
                // )
            })
        })
    })
}

function addEnum(code: CodeBuilder, enumModel: RpcEnumModel) {
    const name = modelName(enumModel.name)
    const options = enumModel.options.map(option => `"${modelName(option)}"`)
    code.addUnionType({name: name, types: options})
        // Add a list of values of an enum to make it easier to use
        .addConst(name + "Values", `[${options.join(", ")}]`)
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

