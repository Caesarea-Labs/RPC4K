import {
    BooleanSerializer,
    NumberSerializer,
    RpcEnumModel,
    RpcModel,
    RpcModelKind,
    RpcStructModel,
    RpcType,
    RpcTypeNames,
    RpcUnionModel,
    StringSerializer
} from "rpc4ts-runtime";
import {CodeBuilder} from "./codegen/CodeBuilder";
import {Rpc4TsClientGenerationOptions} from "./ClientGenerator";
import {buildRecord} from "rpc4ts-runtime/src/impl/Util";
import {modelName} from "./Rpc4tsType";
import {structRuntimeName} from "./ModelGenerator";
import {uniqueBy} from "./Util";

export function libraryPath(path: string, options: Rpc4TsClientGenerationOptions) {
    if (options.localLibPaths) return `../../src/${path}`
    else return "rpc4ts-runtime"
}

export function addSerializerImports(codeBuilder: CodeBuilder, options: Rpc4TsClientGenerationOptions): CodeBuilder {
    return codeBuilder.addImport(
        [
            "BooleanSerializer", "StringSerializer", "NumberSerializer", "ArraySerializer",
            "DayjsSerializer", "DurationSerializer", "NullableSerializer", "VoidSerializer",
            "RecordSerializer", "TupleSerializer"
        ],
        libraryPath("serialization/BuiltinSerializers", options)
    )
        .addImport(["EnumSerializer"], libraryPath("serialization/EnumSerializer", options))
        .addImport(["UnionSerializer"], libraryPath("serialization/UnionSerializer", options))
}

/**
 *
 * @param models
 */
export function generateSerializers(models: RpcModel[], options: Rpc4TsClientGenerationOptions) {

    const modelNames = models.map(model => modelName(model.name))

    const builder = new CodeBuilder()
        .addImport(
            ["TsSerializer"],
            libraryPath("serialization/TsSerializer", options)
        )
        .addImport(
            ["GeneratedSerializerImpl"],
            libraryPath("serialization/GeneratedSerializer", options)
        )
        .addImport(modelNames, "./rpc4ts_AllEncompassingServiceModels")

    addSerializerImports(builder, options)

    const modelMap = buildRecord(models, (model) => [model.name, model])

    for (const model of models) {
        switch (model.type) {
            case RpcModelKind.struct:
                addStructSerializer(builder, model)
                break;
            case RpcModelKind.enum:
                addEnumSerializer(builder, model)
                break;
            case RpcModelKind.inline: {
                // No need to generate anything for inline types, as they are aliases in TS.
                break;
            }
            case RpcModelKind.union:
                addUnionSerializer(builder, model, modelMap)
                break;
        }
    }
    return builder.build()

}

// A map from each model name to its full description
type ModelMap = Record<string, RpcModel>

const TypeParameterPrefix = "T"
const TypeParameterSerializerPrefix = "typeArg"

function addEnumSerializer(code: CodeBuilder, enumModel: RpcEnumModel) {
    const enumName = modelName(enumModel.name)
    code.addTopLevelFunction(serializerName(enumName), [], `TsSerializer<${enumName}>`, (builder) => {
        builder.addReturningFunctionCall("new EnumSerializer", [
            `"${enumName}"`,
            `[${enumModel.options.map(option => `"${option}"`).join(", ")}]`
        ])
    })
}


function addUnionSerializer(code: CodeBuilder, unionModel: RpcUnionModel, modelMap: ModelMap) {
    const unionName = modelName(unionModel.name)
    // Name type arguments 'T{i}`
    const typeArguments = unionModel.typeParameters.length === 0 ? "" :
        "<" + unionModel.typeParameters.map((_, i) => `T${i}`) + ">"
    const modelType = `<${unionName}${typeArguments}>`
    const typeParameterValues = mapTypeParametersValues(unionModel)
    code.addTopLevelFunction(serializerName(unionName), [], `TsSerializer${modelType}`, (builder) => {

        const subclasses = fullyExpandUnion(unionModel, modelMap)
        const uniqueSubclasses = uniqueBy(subclasses, subclass => subclass.name)

        const subclassNames = uniqueSubclasses.map(type => {
            const model = modelMap[type.name]
            if (model === undefined) {
                throw new Error(`Unknown model name: ${type.name}`)
            }
            if (model.type === "enum") {
                throw new Error("Enums are not expected to be part of a union at the moment")
            }
            if (model.type !== "struct") {
                throw new Error(`Unexpected model type: ${model.type}`)
            }
            return structRuntimeName(model)
        })

        const subclassSerializers = uniqueSubclasses.map(type => buildSerializer(type, typeParameterValues))


        builder.addReturningFunctionCall(`new UnionSerializer${modelType}`, [
            `"${unionName}"`,
            `"${unionName}"`, // Not entirely accurate but should work (it should be the runtime name of the union)
            `[${subclassNames.join(", ")}]`,
            `[${subclassSerializers.join(", ")}]`
        ])
    })
}


type NonUnionModels = (RpcType)[]

/**
 * If a union type contains types that are also unions, this will expand those as well.
 * This also expands inline types.
 */
function fullyExpandUnion(union: RpcUnionModel, modelMap: ModelMap): NonUnionModels {
    const arr: NonUnionModels = []
    addUnionChildren(union, arr, modelMap)
    return arr
}

function addUnionChildren(union: RpcUnionModel, to: NonUnionModels, modelMap: ModelMap) {
    for (const option of union.options) {
        addChild(option, to, modelMap)
    }
}

function addChild(type: RpcType, to: NonUnionModels, modelMap: ModelMap) {
    const model = modelMap[type.name]
    if (model === undefined) {
        throw new Error(`Unknown model specified as child of union type: ${type.name}`)
    }
    if (model.type === "union") {
        addUnionChildren(model, to, modelMap)
    } else if (model.type === "inline") {
        addChild(model.inlinedType, to, modelMap)
    } else {
        to.push(type)
    }
}

//     export function testUnion<T>(argSerializer: TsSerializer<T>): TsSerializer<TestUnion<T>> {
//         return new UnionSerializer<TestUnion<T>>("TestUnion",  "TestUnion",
//             ["AnotherModelHolder",  "GenericThing"],
//             [anotherModelHolder(StringSerializer), genericThing(argSerializer, NumberSerializer)]
//         )
//     }

export function serializerName(modelName: string): string {
    return `rpc4ts_serializer_${modelName}`
}


function addStructSerializer(code: CodeBuilder, struct: RpcStructModel) {
    const typeParameterValues = mapTypeParametersValues(struct)
    const structName = modelName(struct.name)
    // Name type argument serializers 'typeArg{i}`
    const parameters: [string, string][] = struct.typeParameters
        .map((_, i) => [`typeArg${i}`, `TsSerializer<${TypeParameterPrefix}${i}>`])
    // Name type arguments 'T{i}`
    const typeArguments = struct.typeParameters.length === 0 ? "" :
        "<" + struct.typeParameters.map((_, i) => `T${i}`) + ">"
    const modelType = `<${structName}${typeArguments}>`
    const serializeName = serializerName(structName)
    code.addTopLevelFunction(`${serializeName}${typeArguments}`, parameters, `TsSerializer${modelType}`, (func) => {
        const serializers = struct.properties.map(prop => {
            const serializer = buildSerializer(prop.type, typeParameterValues)
            // If the serializer refers to itself, defer it to prevent infinite recursion.
            // The GeneratedSerializerImpl takes care to lazily use the serializers so that recursion is limited.
            const deferredSerializer = serializer.includes(serializeName) ? `() => ${serializer}` : serializer
            return `${prop.name}: ${deferredSerializer}`;
        }).join(", ")
        const typeArgumentParamNames = parameters.map(([name]) => name).join(", ")
        func.addReturningFunctionCall(
            `new GeneratedSerializerImpl${modelType}`,
            [
                structRuntimeName(struct),
                `{${serializers}}`,
                `[${typeArgumentParamNames}]`,
                `(params) => new ${structName}(params)`
            ]
        )
    })
}

function mapTypeParametersValues(model: RpcUnionModel | RpcStructModel): Record<string, string> {
    return buildRecord(
        model.typeParameters,
        // For the type parameter T0, use the serializer typeArgSerializer0, and so on.
        (param, i) => [param, TypeParameterSerializerPrefix + i]
    )
}


// }

/**
 * A serializer requires all type parameters to be fulfilled.
 * @param typeParameterValues Since `forType` may have type parameters, we need to know how to assign each type parameter its value.
 */
export function buildSerializer(forType: RpcType, typeParameterValues: Record<string, string>): string {
    return new SerializerBuilder(typeParameterValues).buildSerializer(forType)
}

class SerializerBuilder {
    typeParameterValues: Record<string, string>

    constructor(typeParameterValues: Record<string, string>) {
        this.typeParameterValues = typeParameterValues
    }


    /**
     * Takes into account nullability
     * @param type
     */
    buildSerializer(type: RpcType): string {
        const withoutNullable = this.buildSerializerImpl(type)
        if (type.isNullable) {
            return `new NullableSerializer(${withoutNullable})`
        } else {
            return withoutNullable
        }
    }


    private buildSerializerImpl(type: RpcType): string {
        if (type.isTypeParameter) {
            const parameterValue = this.typeParameterValues[type.name]
            if (parameterValue === undefined) {
                throw new Error(`Type parameter had an unexpected name: ${type.name}`)
            }
            return parameterValue
        }
        if (type.inlinedType !== undefined) {
            // Inline type are serialized with the same serializer as what they are inlined to because they are just aliases
            return this.buildSerializer(type.inlinedType)
        }
        switch (type.name) {
            case "bool" :
                return "BooleanSerializer"
            case "i8":
            case "i16":
            case "i32":
            case "i64":
            case "f32":
            case "f64":
                return "NumberSerializer"
            case "char":
            case "string":
            case "uuid":
                return "StringSerializer"
            case "duration":
                // Durations are Dayjs.Duration in typescript
                return "DurationSerializer"
            case  RpcTypeNames.Time:
                // Dates are Dayjs in typescript
                return "DayjsSerializer"
            case RpcTypeNames.Arr: {
                const typeArgs = type.typeArguments
                if (typeArgs.length !== 1) {
                    throw new Error(`Array type had an unexpected amount of type arguments: ${typeArgs.length}`)
                }

                const elementType = typeArgs[0]
                const elementTypeSerializer = this.buildSerializer(elementType)
                return `new ArraySerializer(${elementTypeSerializer})`
            }
            case RpcTypeNames.Rec : {
                const typeArgs = type.typeArguments
                if (typeArgs.length !== 2) {
                    throw new Error(`Record type had an unexpected amount of type arguments: ${typeArgs.length}`)
                }
                const keySerializer = this.buildSerializer(typeArgs[0])
                const valueSerializer = this.buildSerializer(typeArgs[1])
                // Typescript Records are Record<K,V>
                return `new RecordSerializer(${keySerializer}, ${valueSerializer})`
            }
            case RpcTypeNames.Tuple: {
                // Typescript tuples are [T1, T2, ..., Tn]
                return `new TupleSerializer([${type.typeArguments.map(arg => this.buildSerializer(arg)).join(", ")}])`
            }
            case RpcTypeNames.Void:
                return "VoidSerializer"
            default: {
                const typeArgumentSerializers = type.typeArguments.map(arg => this.buildSerializer(arg))
                    .join(", ")
                return `rpc4ts_serializer_${modelName(type.name)}(${typeArgumentSerializers})`
            }

        }
    }

}

