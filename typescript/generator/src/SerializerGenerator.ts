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
    StringSerializer,
    TupleSerializer
} from "rpc4ts-runtime";
import {CodeBuilder} from "./codegen/CodeBuilder";
import {Rpc4TsClientGenerationOptions} from "./ClientGenerator";
import {buildRecord} from "rpc4ts-runtime/src/impl/Util";
import {structRuntimeName} from "./ModelGenerator";
import {uniqueBy} from "./Util";
import {concat, join, MaybeFormattedString, resolveMaybeFormatString, TsFunction, TsType, TsTypes} from "./codegen/FormatString";
import {modelName2, modelType} from "./Rpc4tsType";

export function libraryPath(path: string, options: Rpc4TsClientGenerationOptions) {
    if (options.localLibPaths) return `../../src/${path}`
    else return "rpc4ts-runtime"
}

// export function addSerializerImports(codeBuilder: CodeBuilder, options: Rpc4TsClientGenerationOptions): CodeBuilder {
//     return codeBuilder.addImport(
//         [
//             "BooleanSerializer", "StringSerializer", "NumberSerializer", "ArraySerializer",
//             "DayjsSerializer", "DurationSerializer", "NullableSerializer", "VoidSerializer",
//             "RecordSerializer", "TupleSerializer"
//         ],
//         libraryPath("serialization/BuiltinSerializers", options)
//     )
//         .addImport(["EnumSerializer"], libraryPath("serialization/EnumSerializer", options))
//         .addImport(["UnionSerializer"], libraryPath("serialization/UnionSerializer", options))
// }

/**
 *
 * @param models
 */
export function generateSerializers(models: RpcModel[], options: Rpc4TsClientGenerationOptions, serviceName: string) {

    // const modelNames = models.map(model => modelName(model.name))

    const builder = new CodeBuilder()
    // .addImport(
    //     ["TsSerializer"],
    //     libraryPath("serialization/TsSerializer", options)
    // )
    // .addImport(
    //     ["GeneratedSerializerImpl"],
    //     libraryPath("serialization/GeneratedSerializer", options)
    // )
    // .addImport(modelNames, "./rpc4ts_AllEncompassingServiceModels")

    // addSerializerImports(builder, options)

    const modelMap = buildRecord(models, (model) => [model.name, model])

    for (const model of models) {
        switch (model.type) {
            case RpcModelKind.struct:
                addStructSerializer(builder, model, serviceName)
                break;
            case RpcModelKind.enum:
                addEnumSerializer(builder, model, serviceName)
                break;
            case RpcModelKind.inline: {
                // No need to generate anything for inline types, as they are aliases in TS.
                break;
            }
            case RpcModelKind.union:
                addUnionSerializer(builder, model, modelMap, serviceName)
                break;
        }
    }
    return builder.build()

}

// A map from each model name to its full description
type ModelMap = Record<string, RpcModel>

const TypeParameterPrefix = "T"
const TypeParameterSerializerPrefix = "typeArg"

function TS_SERIALIZER(type: TsType): TsType {
    return TsTypes.library("TsSerializer", "serialization/TsSerializer", type)
}

const ENUM_SERIALIZER = TsTypes.library("EnumSerializer", "serialization/EnumSerializer")

function addEnumSerializer(code: CodeBuilder, enumModel: RpcEnumModel, serviceName: string) {
    const enumType = modelType(enumModel.name, serviceName)
    const enumName = modelName2(enumModel.name)
    code.addTopLevelFunction(serializerName2(enumModel.name), [], TS_SERIALIZER(enumType), (builder) => {
        builder.addReturningFunctionCall(concat("new ", ENUM_SERIALIZER), [
            `"${enumName}"`,
            `[${enumModel.options.map(option => `"${option}"`).join(", ")}]`
        ])
    })
}

const UNION_SERIALIZER = TsTypes.library("UnionSerializer", "serialization/UnionSerializer")


function addUnionSerializer(code: CodeBuilder, unionModel: RpcUnionModel, modelMap: ModelMap, serviceName: string) {
    const unionName = modelName2(unionModel.name)
    // Name type arguments 'T{i}`
    const typeArguments = unionModel.typeParameters.map((_, i) => TsTypes.typeParameter(TypeParameterPrefix + i))
    // const typeArguments = unionModel.typeParameters.length === 0 ? "" :
    //     "<" + unionModel.typeParameters.map((_, i) => `T${i}`) + ">"
    const unionType = modelType(unionModel.name, serviceName, typeArguments)
    // const modelType2 = `<${unionName}${typeArguments}>`
    const typeParameterValues = mapTypeParametersValues(unionModel)
    code.addTopLevelFunction(serializerDeclaration(unionModel.name, typeArguments), [], TS_SERIALIZER(unionType), (builder) => {

        const subclasses = fullyExpandUnion(unionModel, modelMap)
        const uniqueSubclasses = uniqueBy(subclasses, subclass => subclass.name)

        const subclassNames = uniqueSubclasses.map(type => {
            const model = modelMap[type.name]
            if (model === undefined) {
                throw new Error(`Unknown model name: ${type.name}`)
            }
            if (model.type === RpcModelKind.enum) {
                throw new Error("Enums are not expected to be part of a union at the moment")
            }
            if (model.type !== RpcModelKind.struct) {
                throw new Error(`Unexpected model type: ${model.type}`)
            }
            return structRuntimeName(model)
        })

        const subclassSerializers = uniqueSubclasses.map(type => buildSerializer(type, typeParameterValues, serviceName))


        builder.addReturningFunctionCall(`new UnionSerializer${modelType}`, [
            `"${unionName}"`,
            `"${unionName}"`, // Not entirely accurate but should work because the implementation doesn't actually consider what is passed here
            // (it should be the runtime name of the union)
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
    if (model.type === RpcModelKind.union) {
        addUnionChildren(model, to, modelMap)
    } else if (model.type === RpcModelKind.inline) {
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


export function serializerName2(typeName: string): string {
    return `rpc4ts_serializer_${modelName2(typeName)}`
}

function serializerDeclaration(modelName: string, typeArguments: TsType[]): MaybeFormattedString {
    if (typeArguments.length === 0) return serializerName2(modelName)
    return concat(serializerName2(modelName) + "<", ...typeArguments, ">")
}


function addStructSerializer(code: CodeBuilder, struct: RpcStructModel, serviceName: string) {
    const typeParameterValues = mapTypeParametersValues(struct)
    // const structName = modelName(struct.name)
    // Name type argument serializers 'typeArg{i}`
    const parameters: [string, TsType][] = struct.typeParameters
        .map((_, i) => [`typeArg${i}`, TS_SERIALIZER(TsTypes.typeParameter(TypeParameterPrefix + i))])
    // Name type arguments 'T{i}`
    // const typeArguments = struct.typeParameters.length === 0 ? "" :
    //     "<" + struct.typeParameters.map((_, i) => `T${i}`) + ">"
    const typeArguments = struct.typeParameters.map((_, i) => TsTypes.typeParameter(TypeParameterPrefix + i))

    const structType = modelType(struct.name, serviceName, typeArguments)
    // const structName2 = modelName2(struct.name)

    // const modelType = `<${structName}${typeArguments}>`
    const serializeName = serializerName2(struct.name)
    code.addTopLevelFunction(serializerDeclaration(struct.name, typeArguments), parameters, TS_SERIALIZER(structType), (func) => {
        const serializers = struct.properties.map(prop => {
            const serializer = buildSerializer(prop.type, typeParameterValues, serviceName)
            // If the serializer refers to itself, defer it to prevent infinite recursion.
            // The GeneratedSerializerImpl takes care to lazily use the serializers so that recursion is limited.
            const deferredSerializer = resolveMaybeFormatString(serializer).includes(serializeName) ? concat(`() => `, serializer) : serializer
            return concat(`${prop.name}: `, deferredSerializer);
        }).join(", ")
        const typeArgumentParamNames = parameters.map(([name]) => name).join(", ")
        func.addReturningFunctionCall(
            `new GeneratedSerializerImpl${modelType}`,
            [
                structRuntimeName(struct),
                `{${serializers}}`,
                `[${typeArgumentParamNames}]`,
                concat(`(params) => new `, structType, `(params)`)
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
export function buildSerializer(forType: RpcType, typeParameterValues: Record<string, string>, serviceName: string): MaybeFormattedString {
    return new SerializerBuilder(typeParameterValues, serviceName).buildSerializer(forType)
}

class SerializerBuilder {
    typeParameterValues: Record<string, string>
    serviceName: string

    constructor(typeParameterValues: Record<string, string>, serviceName: string) {
        this.typeParameterValues = typeParameterValues
        this.serviceName = serviceName
    }


    /**
     * Takes into account nullability
     * @param type
     */
    buildSerializer(type: RpcType): MaybeFormattedString {
        const withoutNullable = this.buildSerializerImpl(type)
        if (type.isNullable) {
            return concat("new ", NULLABLE_SERIALIZER, "(", withoutNullable, ")")
        } else {
            return withoutNullable
        }
    }


    private buildSerializerImpl(type: RpcType): MaybeFormattedString {
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
                return BOOLEAN_SERIALIZER
            case "i8":
            case "i16":
            case "i32":
            case "i64":
            case "f32":
            case "f64":
                return NUMBER_SERIALIZER
            case "char":
            case "string":
            case "uuid":
                return STRING_SERIALIZER
            case "duration":
                // Durations are Dayjs.Duration in typescript
                return DURATION_SERIALIZER
            case  RpcTypeNames.Time:
                // Dates are Dayjs in typescript
                return DAYJS_SERIALIZER
            case RpcTypeNames.Arr: {
                const typeArgs = type.typeArguments
                if (typeArgs.length !== 1) {
                    throw new Error(`Array type had an unexpected amount of type arguments: ${typeArgs.length}`)
                }

                const elementType = typeArgs[0]
                const elementTypeSerializer = this.buildSerializer(elementType)
                return concat("new ", ARRAY_SERIALIZER, "(", elementTypeSerializer, ")")
            }
            case RpcTypeNames.Rec : {
                const typeArgs = type.typeArguments
                if (typeArgs.length !== 2) {
                    throw new Error(`Record type had an unexpected amount of type arguments: ${typeArgs.length}`)
                }
                const keySerializer = this.buildSerializer(typeArgs[0])
                const valueSerializer = this.buildSerializer(typeArgs[1])
                // Typescript Records are Record<K,V>
                return concat("new ", RECORD_SERIALIZER, "(", keySerializer, ", ", valueSerializer, ")")
            }
            case RpcTypeNames.Tuple: {
                const typeArgs = type.typeArguments.map(arg => this.buildSerializer(arg))
                // Typescript tuples are [T1, T2, ..., Tn]
                return concat("new ", TUPLE_SERIALIZER, "([", join(typeArgs, ", "), "])")
            }
            case RpcTypeNames.Void:
                return VOID_SERIALIZER
            default: {
                const typeArgumentSerializers = join(
                    type.typeArguments.map(arg => this.buildSerializer(arg)),
                    ", "
                )
                return concat(TsFunction.user(serializerName2(type.name), SERIALIZERS_FILE(this.serviceName)), `(`, typeArgumentSerializers, `)`)
            }

        }
    }

}

const NULLABLE_SERIALIZER = TsTypes.library("NullableSerializer", "serialization/builtinSerializers")
const BOOLEAN_SERIALIZER = TsTypes.library("BooleanSerializer", "serialization/builtinSerializers")
const NUMBER_SERIALIZER = TsTypes.library("NumberSerializer", "serialization/builtinSerializers")
const STRING_SERIALIZER = TsTypes.library("StringSerializer", "serialization/builtinSerializers")
const DURATION_SERIALIZER = TsTypes.library("DurationSerializer", "serialization/builtinSerializers")
const DAYJS_SERIALIZER = TsTypes.library("DayjsSerializer", "serialization/builtinSerializers")
const ARRAY_SERIALIZER = TsTypes.library("ArraySerializer", "serialization/builtinSerializers")
const RECORD_SERIALIZER = TsTypes.library("RecordSerializer", "serialization/builtinSerializers")
const TUPLE_SERIALIZER = TsTypes.library("TupleSerializer", "serialization/BuiltinSerializers")
const VOID_SERIALIZER = TsTypes.library("VoidSerializer", "serialization/BuiltinSerializers")

//             case "bool" :
//                 return "BooleanSerializer"
//             case "i8":
//             case "i16":
//             case "i32":
//             case "i64":
//             case "f32":
//             case "f64":
//                 return "NumberSerializer"
//             case "char":
//             case "string":
//             case "uuid":
//                 return "StringSerializer"
//             case "duration":
//                 // Durations are Dayjs.Duration in typescript
//                 return "DurationSerializer"
//             case  RpcTypeNames.Time:
//                 // Dates are Dayjs in typescript
//                 return "DayjsSerializer"
// //         [
// //             "BooleanSerializer", "StringSerializer", "NumberSerializer", "ArraySerializer",
// //             "DayjsSerializer", "DurationSerializer", "NullableSerializer", "VoidSerializer",
// //             "RecordSerializer", "TupleSerializer"
// //         ],
// //         libraryPath("serialization/BuiltinSerializers", options)
// //     )
// //         .addImport(["EnumSerializer"], libraryPath("serialization/EnumSerializer", options))
// //         .addImport(["UnionSerializer"], libraryPath("serialization/UnionSerializer", options))

export function SERIALIZERS_FILE(serviceName: string): string {
    return `./rpc4ts_${serviceName}Serializers`
}
