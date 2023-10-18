import {CodeBuilder} from "./CodeBuilder";
import {typescriptRpcType} from "./TypescriptRpcType";
import {
    RpcEnumModel,
    RpcModel,
    RpcModelKind,
    RpcStructModel,
    RpcTypeDiscriminator,
    RpcUnionModel
} from "../runtime/ApiDefinition";

export function generateModels(models: RpcModel[]): string {
    const builder = new CodeBuilder()
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
    code.addInterface({name: struct.name, typeParameters: struct.typeParameters}, interfaceBuilder => {
        struct.properties.forEach(({name, type, isOptional}) => {
            interfaceBuilder.addProperty(
                {
                    // "type" is a reserved
                    name, optional: isOptional, type: name === RpcTypeDiscriminator ? `"${struct.name}"` : typescriptRpcType(type)
                }
            )
        })
    })
}

function addEnum(code: CodeBuilder, enumModel: RpcEnumModel) {
    code.addUnionType({name: enumModel.name, types: enumModel.options.map(option => `"${option}"`)})
}

function addUnion(code: CodeBuilder, struct: RpcUnionModel) {
    code.addUnionType({
        name: struct.name,
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