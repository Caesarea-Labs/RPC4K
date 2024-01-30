import {RpcEnumModel, RpcModel, RpcModelKind, RpcStructModel, RpcType, RpcUnionModel} from "rpc4ts-runtime";
// import {CodeBuilder} from "./codegen/CodeBuilder";
import {Rpc4TsClientGenerationOptions} from "./ClientGenerator"
// import {buildRecord} from "rpc4ts-runtime";
import {structRuntimeName} from "./ModelGenerator"
import {concat, join, MaybeFormattedString, resolveMaybeFormatString, TsFunction, TsType, TsTypes} from "./codegen/FormatString"
import {modelName, modelType} from "./Rpc4tsType"
import "ts-minimum"
import {CodeBuilder} from "./codegen/CodeBuilder"
import {RpcTypeNames} from "./RpcTypeUtils";


/**
 *
 * @param models
 */
export function generateSerializers(models: RpcModel[], options: Rpc4TsClientGenerationOptions, serviceName: string) {
    const builder = new CodeBuilder(options.localLibPaths)
        .ignoreAnyWarnings()

    const modelMap = models.toRecord((model) => [model.name, model])

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
    const enumName = modelName(enumModel.name)
    code.addTopLevelFunction(serializerName(enumModel.name), [], [], TS_SERIALIZER(enumType), (builder) => {
        builder.addReturningFunctionCall(concat("new ", ENUM_SERIALIZER), [
            `"${enumName}"`,
            `[${enumModel.options.map(option => `"${option}"`).join(", ")}]`
        ])
    })
}

const UNION_SERIALIZER = TsTypes.library("UnionSerializer", "serialization/UnionSerializer")


function addUnionSerializer(code: CodeBuilder, unionModel: RpcUnionModel, modelMap: ModelMap, serviceName: string) {
    const unionName = modelName(unionModel.name)
    // Name type arguments 'T{i}`
    const typeArguments = unionModel.typeParameters.map((_, i) => TsTypes.typeParameter(TypeParameterPrefix + i))
    const unionType = modelType(unionModel.name, serviceName, typeArguments)
    const typeParameterValues = mapTypeParametersValues(unionModel)
    code.addTopLevelFunction(serializerName(unionModel.name), typeArguments, [], TS_SERIALIZER(unionType), (builder) => {

        const subclasses = fullyExpandUnion(unionModel, modelMap)
        const uniqueSubclasses = subclasses.distinctBy(subclass => subclass.name)

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

        // BLOCKED: once we don't need full type names we can remove this
        const fullTypeMap = "{" +
            subclassNames.map(name => `"${name.removeBeforeLastExclusive(".")}": "${name}"`).join(",")
            + "}"

        builder.addReturningFunctionCall(concat("new ", UNION_SERIALIZER, "<", unionType, ">"), [
            `"${unionName}"`,
            `"${unionName}"`, // Not entirely accurate but should work because the implementation doesn't actually consider what is passed here
            // (it should be the runtime name of the union)
            `[${subclassNames.map(name => `"${name}"`).join(", ")}]`,
            concat("[", join(subclassSerializers, ", "), "]"),
            fullTypeMap
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
        addChild(option, to, modelMap, union.name)
    }
}

function addChild(type: RpcType, to: NonUnionModels, modelMap: ModelMap, parentName: string) {
    const model = modelMap[type.name]
    if (model === undefined) {
        throw new Error(`Unknown model '${type.name}' specified as child of union type '${parentName}' (this happens when you have a sealed superinterface of an inline class, it's not supported yet)`)
    }
    if (model.type === RpcModelKind.union) {
        addUnionChildren(model, to, modelMap)
    } else if (model.type === RpcModelKind.inline) {
        addChild(model.inlinedType, to, modelMap, type.name)
    } else {
        to.push(type)
    }
}

export function serializerName(typeName: string): string {
    return `rpc4ts_serializer_${modelName(typeName)}`
}


function addStructSerializer(code: CodeBuilder, struct: RpcStructModel, serviceName: string) {
    const typeParameterValues = mapTypeParametersValues(struct)
    // Name type argument serializers 'typeArg{i}`
    const parameters: [string, TsType][] = struct.typeParameters
        .map((_, i) => [`typeArg${i}`, TS_SERIALIZER(TsTypes.typeParameter(TypeParameterPrefix + i))])
    // Name type arguments 'T{i}`
    const typeArguments = struct.typeParameters.map((_, i) => TsTypes.typeParameter(TypeParameterPrefix + i))

    const structType = modelType(struct.name, serviceName, typeArguments)

    const serializeName = serializerName(struct.name)
    code.addTopLevelFunction(serializerName(struct.name), typeArguments, parameters, TS_SERIALIZER(structType), (func) => {
        const serializers = join(struct.properties.map(prop => {
            const serializer = buildSerializer(prop.type, typeParameterValues, serviceName)
            // If the serializer refers to itself, defer it to prevent infinite recursion.
            // The GeneratedSerializerImpl takes care to lazily use the serializers so that recursion is limited.
            const deferredSerializer = resolveMaybeFormatString(serializer).includes(serializeName) ? concat(`() => `, serializer) : serializer
            return concat(`${prop.name}: `, deferredSerializer);
        }), ", ")
        const typeArgumentParamNames = parameters.map(([name]) => name).join(", ")
        const args = [
            `"${structRuntimeName(struct)}"`,
            concat("{", serializers, "}"),
            `[${typeArgumentParamNames}]`,
        ]
        // Last parameter is optional and specifies the type discriminator
        if (struct.hasTypeDiscriminator) args.push(`"${struct.name.removeBeforeLastExclusive(".")}"`)
        func.addReturningFunctionCall(
            concat("new ", GENERATED_SERIALIZER_IMPL, "<", structType, ">"),
            args
        )
    })
}

const GENERATED_SERIALIZER_IMPL = TsTypes.library("GeneratedSerializerImpl", "serialization/GeneratedSerializer")

function mapTypeParametersValues(model: RpcUnionModel | RpcStructModel): Record<string, string> {
    return model.typeParameters.toRecord(
        // For the type parameter T0, use the serializer typeArgSerializer0, and so on.
        (param, i) => [param, TypeParameterSerializerPrefix + i]
    )
}

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
            case "i8array":
                return I8ARRAY_SERIALIZER
            case "ui8array":
                return UI8ARRAY_SERIALIZER
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
                return concat(TsFunction.user(serializerName(type.name), SERIALIZERS_FILE(this.serviceName)), `(`, typeArgumentSerializers, `)`)
            }

        }
    }

}

const NULLABLE_SERIALIZER = TsTypes.library("NullableSerializer", "serialization/BuiltinSerializers")
const BOOLEAN_SERIALIZER = TsTypes.library("BooleanSerializer", "serialization/BuiltinSerializers")
const NUMBER_SERIALIZER = TsTypes.library("NumberSerializer", "serialization/BuiltinSerializers")
const STRING_SERIALIZER = TsTypes.library("StringSerializer", "serialization/BuiltinSerializers")
const DURATION_SERIALIZER = TsTypes.library("DurationSerializer", "serialization/BuiltinSerializers")
const DAYJS_SERIALIZER = TsTypes.library("DayjsSerializer", "serialization/BuiltinSerializers")
const ARRAY_SERIALIZER = TsTypes.library("ArraySerializer", "serialization/BuiltinSerializers")
const RECORD_SERIALIZER = TsTypes.library("RecordSerializer", "serialization/BuiltinSerializers")
const TUPLE_SERIALIZER = TsTypes.library("TupleSerializer", "serialization/BuiltinSerializers")
const VOID_SERIALIZER = TsTypes.library("VoidSerializer", "serialization/BuiltinSerializers")
const I8ARRAY_SERIALIZER = TsTypes.library("Int8ArraySerializer", "serialization/BuiltinSerializers")
const UI8ARRAY_SERIALIZER = TsTypes.library("UInt8ArraySerializer", "serialization/BuiltinSerializers")

export function SERIALIZERS_FILE(serviceName: string): string {
    return `./rpc4ts_${serviceName}Serializers`
}
