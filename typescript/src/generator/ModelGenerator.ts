import {RpcModel, RpcType} from "./ApiDefinition";
import {CodeBuilder} from "./CodeBuilder";
import {recordForEach} from "./Utils"
import {typescriptRpcType, typescriptRpcTypeIgnoreOptional} from "./TypescriptRpcType";

export function generateModels(models: RpcModel[]): string {
    const builder = new CodeBuilder()
    for (const model of models) {
        switch (model.type) {
            case RpcModel.Type.struct:
                addStruct(builder, model)
                break;
            case RpcModel.Type.enum:
                addEnum(builder, model)
                break;
            case RpcModel.Type.union:
                addUnion(builder, model)
                break;

        }
    }
    return builder.build()
}

function addStruct(code: CodeBuilder, struct: RpcModel.Struct) {
    code.addInterface({name: struct.name, typeParameters: struct.typeParameters}, interfaceBuilder => {
        recordForEach(struct.properties, (name, type) => {
            interfaceBuilder.addProperty({name, optional: type.isOptional, type: typescriptRpcTypeIgnoreOptional(type)})
        })
    })
}

function addEnum(code: CodeBuilder, enumModel: RpcModel.Enum) {
    code.addUnionType({name: enumModel.name, types: enumModel.options.map(option => `"${option}"`)})
}

function addUnion(code: CodeBuilder, struct: RpcModel.Union) {
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