import {CodeBuilder} from "./CodeBuilder";
import {modelName, simpleModelName, typescriptRpcType} from "./TypescriptRpcType";
import {
    RpcEnumModel,
    RpcModel,
    RpcModelKind,
    RpcStructModel,
    RpcUnionModel
} from "../runtime/impl/ApiDefinition";
import {RpcTypeDiscriminator} from "../runtime/impl/RpcTypeUtils";

export function generateModels(models: RpcModel[]): string {
    const builder = new CodeBuilder()
        .addImport(["Dayjs"], `dayjs`)

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
                    name, optional: isOptional, type: name === RpcTypeDiscriminator ? `"${simpleModelName(struct.name)}"` : typescriptRpcType(type)
                }
            )
        })
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


// @Serializable
// data class EveryBuiltinType(
//     val n: Map<Int,Int>,
//     val o: Set<Int>,
//     val p: Pair<Int,Int>,
//     val q: Triple<Int,Int,Int>,
//     val r: Unit
// ) {

////interface Dog {
// //     name: string
// //     type: string
// //     age: number
// // }