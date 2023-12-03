import {BooleanSerializer, NumberSerializer, RpcModel, RpcModelKind, RpcStructModel, RpcType, RpcTypeNames, StringSerializer} from "rpc4ts-runtime";
import {CodeBuilder} from "./CodeBuilder";
import {Rpc4TsClientGenerationOptions} from "./ClientGenerator";
import {buildRecord} from "rpc4ts-runtime/src/impl/Util";
import {modelName} from "./Rpc4tsType";

/**
 *
 * @param models
 */
export function generateSerializers(models: RpcModel[], options: Rpc4TsClientGenerationOptions) {
    function libraryPath(path: string): string {
        if (options.localLibPaths) return `../../src/${path}`
        else return "rpc4ts-runtime"
    }

    const modelNames = models.map(model => modelName(model.name))

    const builder = new CodeBuilder()
        .addImport(
            [
                "BooleanSerializer", "StringSerializer", "NumberSerializer", "ArraySerializer",
                "DayjsSerializer", "DurationSerializer", "NullableSerializer", "VoidSerializer",
                "RecordSerializer", "HeterogeneousArraySerializer"
            ],
            libraryPath("serialization/BuiltinSerializers")
        )
        .addImport(
            ["TsSerializer"],
            libraryPath("serialization/TsSerializer")
        )
        .addImport(
            ["GeneratedSerializerImpl"],
            libraryPath("serialization/GeneratedSerializer")
        )
        .addImport(modelNames, "./rpc4ts_AllEncompassingServiceModels")


    const modelMap = buildRecord(models, (model) => [model.name, model])
    // .addImport(["Duration"], `dayjs/plugin/duration`)

    for (const model of models) {
        switch (model.type) {
            case RpcModelKind.struct:
                new SerializerGenerator(model,modelMap).addStructSerializer(builder)
                break;
            // case RpcModelKind.enum:
            //     addEnum(builder, model)
            //     break;
            // case RpcModelKind.union:
            //     addUnion(builder, model)
            //     break;
            // case RpcModelKind.inline:
            //     addInlineType(builder, model)

        }
    }
    return builder.build()

}

// A map from each model name to its full description
type ModelMap = Record<string, RpcModel>
// A map from each type parameter of the given struct to the index of that type parameter
type TypeParameterIndices = Record<string, number>

const TypeParameterPrefix = "T"
const TypeParameterSerializerPrefix = "typeArg"

class SerializerGenerator {
    modelMap: ModelMap
    typeParameterIndices: TypeParameterIndices
    struct: RpcStructModel

    constructor(struct: RpcStructModel, modelMap: ModelMap) {
        this.struct = struct
        this.modelMap = modelMap
        this.typeParameterIndices = buildRecord(struct.typeParameters, ((param, i) => [param, i]))
    }

    addStructSerializer(code: CodeBuilder) {
        const struct = this.struct
        const structName = modelName(struct.name)
        // Name type argument serializers 'typeArg{i}`
        const parameters: [string, string][] = struct.typeParameters
            .map((param, i) => [`typeArg${i}`, `TsSerializer<${TypeParameterPrefix}${i}>`])
        // Name type arguments 'T{i}`
        const typeArguments = struct.typeParameters.length === 0 ? "" :
            "<" + struct.typeParameters.map((_, i) => `T${i}`) + ">"
        const modelType = `<${structName}${typeArguments}>`
        const serializerName = `rpc4ts_serializer_${structName}`
        code.addTopLevelFunction(`${serializerName}${typeArguments}`, parameters, `TsSerializer${modelType}`, (func) => {
            const serializers = struct.properties.map(prop => {
                const serializer = this.buildSerializer(prop.type)
                // If the serializer refers to itself, defer it to prevent infinite recursion.
                // The GeneratedSerializerImpl takes care to lazily use the serializers so that recursion is limited.
                const deferredSerializer = serializer.includes(serializerName) ? `() => ${serializer}`: serializer
                return `${prop.name}: ${deferredSerializer}`;
            }).join(", ")
            const typeArgumentParamNames = parameters.map(([name]) => name).join(", ")
            func.addReturningFunctionCall(
                `new GeneratedSerializerImpl${modelType}`,
                [
                    `"${structName}"`,
                    `{${serializers}}`,
                    `[${typeArgumentParamNames}]`,
                    `(params) => new ${structName}(params)`
                ]
            )
        })
    }


    /**
     * Takes into account nullability
     * @param type
     */
    private buildSerializer(type: RpcType): string {
        const withoutNullable = this.buildSerializerImpl(type)
        if (type.isNullable) {
            return `new NullableSerializer(${withoutNullable})`
        } else {
            return withoutNullable
        }
    }


    private buildSerializerImpl(type: RpcType): string {
        if (type.isTypeParameter) {
            const parameterIndex = this.typeParameterIndices[type.name]
            if (parameterIndex === undefined) {
                throw new Error(`Type parameter had an unexpected name: ${type.name}`)
            }
            // For the type parameter T1, use the serializer typeArgSerializer1, and so on.
            return TypeParameterSerializerPrefix + parameterIndex
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
                return `new HeterogeneousArraySerializer([${type.typeArguments.map(arg => this.buildSerializer(arg)).join(", ")}])`
            }
            case RpcTypeNames.Void:
                return "VoidSerializer"
            default: {
                return this.resolveModelSerializer(type)
            }

        }
    }

    private resolveModelSerializer(modelType: RpcType): string {
        const modelName = modelType.name
        const model = this.modelMap[modelName]
        if (model === undefined) {
            throw new Error(`Can't resolve serializer for type '${modelName}' as it's not a builtin type nor an existing model`)
        }
        switch (model.type) {
            case RpcModelKind.enum:
                throw new Error("TODO")
            case RpcModelKind.struct:
                const propertySerializers = modelType.typeArguments.map(arg => this.buildSerializer(arg))
                    .join(", ")
                return `rpc4ts_serializer_${modelName}(${propertySerializers})`
            case RpcModelKind.union:
                throw new Error("TODO")
            case RpcModelKind.inline:
                // We treat inline types as aliases in typescript so we can just have a serializer for the underlying type.
                return this.buildSerializer(model.inlinedType)
        }
    }

}


// export namespace Rpc4tsSerializers {
//     export function anotherModelHolder<T>(typeArgument1: TsSerializer<T>): TsSerializer<AnotherModelHolder<T>> {
//         return new GeneratedSerializer<AnotherModelHolder<T>>(
//             "AnotherModelHolder",
//             {t: genericThing(typeArgument1, StringSerializer)},
//             [typeArgument1],
//             (params) => new AnotherModelHolder(params)
//         )
//     }
//
//     export function genericThing<T1, T2>(typeArgument1: TsSerializer<T1>, typeArgument2: TsSerializer<T2>): TsSerializer<GenericThing<T1, T2>> {
//         return new GeneratedSerializer<GenericThing<T1, T2>>(
//             "GenericThing",
//             {x: typeArgument1, w: new ArraySerializer(typeArgument2), a: StringSerializer},
//             [typeArgument1],
//             (params) => new GenericThing(params)
//         )
//     }
//
//     export function foo(): TsSerializer<Foo> {
//         return new GeneratedSerializer<Foo>(
//             "Foo",
//             {x: NumberSerializer, y: StringSerializer, z: BooleanSerializer},
//             [],
//             (params) => new Foo(params)
//         )
//     }
// }

