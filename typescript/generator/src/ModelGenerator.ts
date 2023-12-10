import {CodeBuilder} from "./codegen/CodeBuilder";
import {RpcEnumModel, RpcInlineModel, RpcModel, RpcModelKind, RpcStructModel, RpcType, RpcUnionModel,} from "rpc4ts-runtime";
import {modelName2, modelType, typescriptRpcType} from "./Rpc4tsType";
import {TsObjectType, TsTypes} from "./codegen/FormatString";
import {Rpc4TsClientGenerationOptions} from "./ClientGenerator";

export function generateModels(models: RpcModel[], serviceName: string, options: Rpc4TsClientGenerationOptions): string {
    const builder = new CodeBuilder(options.localLibPaths)
        // .addImport(["Dayjs"], `dayjs`)
        // .addImport(["Duration"], `dayjs/plugin/duration`)

    for (const model of models) {
        switch (model.type) {
            case RpcModelKind.struct:
                addStruct(builder, model, serviceName)
                break;
            case RpcModelKind.enum:
                addEnum(builder, model)
                break;
            case RpcModelKind.union:
                addUnion(builder, model, serviceName)
                break;
            case RpcModelKind.inline:
                addInlineType(builder, model, serviceName)

        }
    }
    return builder.build()
}

/**
 * Classes are used over interfaces because they allow greater control over what is constructed and exposed.
 * In optional properties, we can allow clients to construct them without specifying the property value, but still expose the
 * property as a non-nullable value assuming the server always gives it a value.
 */
function addStruct(code: CodeBuilder, struct: RpcStructModel, serviceName :string) {
    const name = modelName2(struct.name)
    code.addClass({name, typeParameters: struct.typeParameters}, classBuilder => {
        struct.properties.forEach(({name, type, isOptional}) => {
            classBuilder.addProperty(
                //TODO: handle isOptional properly
                {
                    name: `readonly ${name}`, optional: isOptional, type: typescriptRpcType(type, serviceName)
                }
            )
        })


        // Add type discriminator if needed with a string type that can only be a specific value that is the model name
        // if (struct.hasTypeDiscriminator) {
        //     classBuilder.addProperty({name: RpcTypeDiscriminator, optional: false, type: `"${simpleModelName(struct.name)}"`})
        // }

        const propertyNames = struct.properties.map(property => property.name).join(", ")
        const propertyTypes : TsObjectType = {
            properties: struct.properties.toRecord(property => [property.name,typescriptRpcType(property.type, serviceName)])
        }
        // const propertyTypes = struct.properties.map(property => `${property.name}: ${typescriptRpcType(property.type)}`).join(", ")
        // Only one parameter: the object argument
        classBuilder.addConstructor([[`{${propertyNames}}`, propertyTypes]], ctrBuilder => {
            struct.properties.forEach(({name, type, isOptional}) => {
                //TODO: handle isOptional

                ctrBuilder.addAssignment(`this.${name}`, `${name}`)

                // classBuilder.addProperty(
                //     {
                //         // "type" is a reserved, and we use the simple model name to make the code more ergonomic
                //         name: `readonly ${name}`, optional: isOptional, type: typescriptRpcType(type)
                //     }
                // )
            })
        })

        // Hide the brand way below
        classBuilder.addNewline()
            .addComment("@ts-ignore - require using constructor")

        // Add _brand property to prevent random objects being assignable to this class
        classBuilder.addProperty({name: "private readonly _brand", type: TsTypes.VOID})

        if (struct.hasTypeDiscriminator) {
            // We inject _rpc_name fields in classes that we may want to get the type name of in runtime.
            classBuilder.addProperty({name: "private readonly _rpc_name", initializer: structRuntimeName(struct)})
        }
    })
}




// This is the same format that kotlinx.serialization uses, for now.
export function structRuntimeName(model: RpcStructModel): string {
    const packageNamePrefix = model.packageName !== "" ? `${model.packageName}.` : ""
    // BLOCKED: get rid of package name shenanigans
    return `"${packageNamePrefix}${model.name}"`
}

function addEnum(code: CodeBuilder, enumModel: RpcEnumModel) {
    const name = modelName2(enumModel.name)
    const options = enumModel.options.map(option => `"${option}"`)
    code.addUnionType({name: name, types: options.map(option => TsTypes.stringLiteral(option))})
        // Add a list of values of an enum to make it easier to use
        .addConst(name + "Values", `[${options.join(", ")}]`)
}

function addUnion(code: CodeBuilder, struct: RpcUnionModel, serviceName: string) {
    code.addUnionType({
        name: modelName2(struct.name),
        types: struct.options.map(option => typescriptRpcType(option, serviceName)),
        typeParameters: struct.typeParameters
    })
}


function addInlineType(code: CodeBuilder, model: RpcInlineModel, serviceName: string) {
    code.addTypeAlias({
        name: modelName2(model.name),
        type: typescriptRpcType(model.inlinedType, serviceName),
        typeParameters: model.typeParameters
    })
}

