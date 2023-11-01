import {RpcModel, RpcModelKind, RpcStructModel, RpcType, RpcUnionModel} from "./ApiDefinition";
import {buildRecord, objectForEach, objectMapValues, removeBeforeLastExclusive} from "./Util";
import dayjs, {isDayjs} from "dayjs";
import {RpcTypeDiscriminator, RpcTypeNames, RpcTypes} from "./RpcTypeUtils";
import {simpleModelName} from "./ModelName";
import duration from "dayjs/plugin/duration";

dayjs.extend(duration)

interface TypeAdapterContext {
    /**
     * If the value is a property of an object, will evaluate to the property name.
     */
    key?: string
    /**
     * The value that is being adapted
     */
    value: unknown
    /**
     * The RpcType of the value
     */
    type: RpcType
    /**
     * If the type is a user-defined model, will evaluate to that model.
     */
    model?: RpcModel | undefined
    //TODO: move packageName to RpcModel instead of RpcType
    /**
     * If this value is part of a struct, this will be the model object of that struct.
     */
    parentModel?: RpcModel | undefined
    /**
     *  Whether a type discriminator is required because the parent of this is a member of a union type (also true when the type itself is part of a union type)
     */
    polymorphic?: boolean
}

// function typeContext(context: {key?})

/**
 * Allow transforming any value while knowing its type.
 * Return undefined to use the original value.
 * @param key
 * @param parentType If the value is a property of an object, will evaluate to the type of that object.
 * @param polymorphic Whether a 'type' field is required because the parent of this is a member of a union type (also true when the type itself is part of a union type)
 */
type TypedValueAdapter = (context: TypeAdapterContext) => unknown


const DefaultJavascriptToSpecAdapter: TypedValueAdapter = (context) => {
    // The rpc4k spec defines all "void" typed values to be equal to "void"
    if (context.type.name === RpcTypeNames.Void) return "void"
    // dayjs is automatically converted to iso-string with json

    // Dayjs duration needs to be converted to iso string explicitly?
    // if (dayjs.isDuration(value)) return value.toISOString()

    const parentModel = context.parentModel
    if (parentModel?.type === "struct" && parentModel.hasTypeDiscriminator && context.key === RpcTypeDiscriminator) {
        if (context.polymorphic === true) {
            // Currently, because kotlin is being a bitch, we need to provide te package name in addition to the class name for the type discriminator.
            return parentModel.packageName + "." + parentModel.name
        } else {
            // This branch is reached when you have a type that is sometimes polymorphic because it's part of a union type,
            // but in this case it is used in a non-polymorphic context, directly, instead of part of a union type.
            // The "type" field is not allowed when not in the context of union types.
            // Return undefined to eliminate the "type" field.
            return undefined
        }
    }

    return context.value
}
const DefaultSpecToJavascriptAdapter: TypedValueAdapter = ({key, value, parentModel, model, polymorphic, type}) => {
    // Typescript defines all values typed "void" will be either undefined or null.
    if (type.name === RpcTypeNames.Void) return undefined

    // Date values are deserialized to dayjs (from iso strings)
    if (type.name === RpcTypeNames.Time) {
        if (typeof value !== "string") {
            throw new Error(`Unexpected non-string as a date value: ${value} of type ${typeof value}`)
        }
        return dayjs(value)
    }
    // Duration values are deserialized to dayjs.Duration (from iso strings)
    if (type.name === RpcTypeNames.Dur) {
        if (typeof value !== "string") {
            throw new Error(`Unexpected non-string as a duration value: ${value} of type ${typeof value}`)
        }
        return dayjs.duration(value)
    }

    // This is in the case that this is the type discriminator itself
    const parentHasTypeDiscriminator = parentModel?.type === "struct" && parentModel.hasTypeDiscriminator
    // Typescript still expects the simple name to be the value of the type field
    if (parentHasTypeDiscriminator && key === RpcTypeDiscriminator) {
        return simpleModelName(parentModel.name)
    }

    // This is in the case this is a struct that contains a type discriminator
    const hasTypeDiscriminator = model?.type === "struct" && model.hasTypeDiscriminator
    // Even if in this case the type is not polymorphic, we want to include the "type" field anyway because it's vital for typescript code using it.
    if (polymorphic !== true && hasTypeDiscriminator) {
        if (typeof value !== "object") throw new Error("Unexpected non-object value with a struct type")
        // Add the "type" because the server might omit it
        return {...value, type: simpleModelName(type.name)}
    }
    return value
}


/**
 * Sometimes the values Stringify.json gives aren't quite write, like dates have special handling and void has special handling
 * this takes care to adapt the values to their correct types, according to the model specified by the server.
 */
export class Rpc4aTypeAdapter {
    private readonly modelMap: Record<string, RpcModel>

    constructor(models: RpcModel[]) {
        this.modelMap = buildRecord(models, (model) => [model.name, model])
    }

    alignJsItemWithRpcSpec(value: unknown, type: RpcType): unknown {
        return new RpcTypeAdapterImpl(this.modelMap, DefaultJavascriptToSpecAdapter).alignWithType({value, type})
    }

    alignRpcSpecItemWithJs(value: unknown, type: RpcType): unknown {
        return new RpcTypeAdapterImpl(this.modelMap, DefaultSpecToJavascriptAdapter).alignWithType({value, type})
    }

}


/**
 * Uses a single TypedValueAdapter
 */
class RpcTypeAdapterImpl {
    private readonly modelMap: Record<string, RpcModel>
    private readonly adapter: TypedValueAdapter;

    constructor(models: Record<string, RpcModel>, adapter: TypedValueAdapter) {
        this.modelMap = models
        this.adapter = adapter;
    }

    /**
     * @param propertyName In case the item is a value of some property, propertyName should specify what the property key is.
     * @param parentType In case the item is a value of some property in some object, this will be the type of that object
     * @param polymorphic True if either this type is part of union type, or it is inside a struct model that is part of a union type.
     */
    alignWithType(context: TypeAdapterContext): unknown {
        // matchingModel is not relevant when the originalItem is not an object
        const matchingModel = this.getModelOfType(context.type)
        //         const item = adapter(propertyName, originalItem, type, matchingModel, parentType, isPolymorphic)
        const value = this.adapter({...context, model: matchingModel})
        // Don't allow further processing on dayjs objects
        if (isDayjs(value) || dayjs.isDuration(value)) return value
        if (value === null) return null

        const type = context.type
        if (typeof value === "object") {
            //NiceToHave: Support complex keys in Typescript
            // if(item instanceof HashMap) {
            //       ...
            // }
            if (matchingModel !== undefined) {
                // This item is one of the models
                switch (matchingModel.type) {
                    case RpcModelKind.enum:
                        throw new Error(`Item ${JSON.stringify(value)} is invalid: it's an object but it's defined as an enum (string union)`)
                    case RpcModelKind.struct:
                        return this.alignStructWithModel(value, matchingModel, context.polymorphic)
                    case RpcModelKind.union: {
                        // Need to do a bit of extra work with union type to know what the actual type is in this case
                        const actualType = resolveActualMemberOfUnionType(value, matchingModel)
                        // Important: Specify polymorphic as true
                        return this.alignWithType({value, type: actualType, polymorphic: true})
                    }
                    case RpcModelKind.inline:
                        // Inline: delegate handling to the inlined type
                        return this.alignWithType({...context, type: matchingModel.inlinedType})
                }
            } else {
                if (Array.isArray(value)) {
                    // This item is an array or a tuple
                    if (type.name === RpcTypeNames.Tuple) {
                        return this.alignTupleWithTypeArguments(value, type);
                    } else if (type.name === RpcTypeNames.Arr) {
                        return this.alignArrayWithTypeArgument(value, type)
                    } else {
                        throw new Error(`Item is invalid: it's an array but not it's defined as such,` +
                            ` it's actually defined as a ${type.name}. Item: ${JSON.stringify(value)}`)
                    }
                } else {
                    return this.alignRecordWithKeyValues(value, type)
                }
            }
        } else {
            return value
        }
    }

    /**
     * Expands the type parameters of the model of the given name with the given type arguments
     */
    private getModelOfType(type: RpcType): RpcModel | undefined {
        const matchingModel = this.modelMap[type.name]
        if (matchingModel === undefined) return undefined
        if (matchingModel.type === RpcModelKind.enum) return matchingModel // Enums don't have type arguments
        const parameterValues = buildRecord(
            // Populate type parameter values by index - the first argument corresponds to the first parameter, and so on.
            type.typeArguments, (typeArg, i) => [matchingModel.typeParameters[i], typeArg]
        )
        return expandModelTypeParameters(matchingModel, parameterValues);
    }


    private alignTupleWithTypeArguments(item: unknown[], type: RpcType) {
        if (item.length !== type.typeArguments.length) {
            throw new Error(`Tuple is invalid, it's supposed to have ${type.typeArguments.length}` +
                ` elements, but it actually has ${item.length} elements. Tuple: ${JSON.stringify(item)}`)
        }
        return item.map((element, i) => {
            const elementType = type.typeArguments[i]
            return this.alignWithType({value: element, type: elementType})
        })
    }

    private alignArrayWithTypeArgument(item: unknown[], type: RpcType) {
        // The RpcTypes are always trustworthy, we don't validate them. We know it has exactly one element.
        const elementType = type.typeArguments[0]
        return item.map(element => this.alignWithType({value: element, type: elementType}))
    }


    private alignStructWithModel(obj: object, model: RpcStructModel, polymorphic: boolean | undefined): object {
        if (Array.isArray(obj)) {
            throw new Error(`Item ${JSON.stringify(obj)} is invalid: it's an array but it's defined as a struct (object)`)
        }
        return objectMapValues(obj, (key, value) => {
            const isTypeDiscriminator = model.hasTypeDiscriminator && key === RpcTypeDiscriminator

            // SLOW: O(n) search on every object property
            const propertyType= isTypeDiscriminator ? RpcTypes.Str
                : model.properties.find(property => property.name === key)?.type

            // Don't transform the type discriminator if it exists
            // if (model.hasTypeDiscriminator && key === RpcTypeDiscriminator) return value
            if (propertyType === undefined) throw new Error(`Item ${JSON.stringify(obj)} is invalid: it doesn't exist in the defined struct (interface): ${JSON.stringify(model)}`)
            // Important: pass the key of the property to the alignWithType and the type of the parent object
            return this.alignWithType({value, type: propertyType, key, parentModel: model, polymorphic})
        })
    }

    private alignRecordWithKeyValues(obj: object, type: RpcType): object {
        if (type.name !== RpcTypeNames.Rec) {
            throw new Error(`Item is invalid: it's an object type but its expected type of '${type.name}'` +
                ` Is not a defined object or array type. Item: ${JSON.stringify(obj)}`)
        }
        // The RpcTypes are always trustworthy, we don't validate them. We know it has exactly two elements.
        const [keyType, valueType] = type.typeArguments
        const resultObject: Partial<Record<string | number, unknown>> = {}

        objectForEach(obj, (key, value) => {
            // Align both keys and values
            const alignedKey = this.alignWithType({value: key, type: keyType}) as string | number
            resultObject[alignedKey] = this.alignWithType({value, type: valueType})
        })

        return resultObject
    }
}

function expandModelTypeParameters(matchingModel: RpcModel, parameterValues: Record<string, RpcType>): RpcModel {
    switch (matchingModel.type) {
        case RpcModelKind.enum:
            return matchingModel
        case RpcModelKind.struct:
            return {
                type: matchingModel.type,
                typeParameters: matchingModel.typeParameters,
                name: matchingModel.name,
                packageName: matchingModel.packageName,
                properties: matchingModel.properties.map(property => {
                    return {
                        name: property.name,
                        isOptional: property.isOptional,
                        type: expandTypeParameters(property.type, parameterValues)
                    }
                }),
                hasTypeDiscriminator: matchingModel.hasTypeDiscriminator
            }
        case RpcModelKind.union:
            return {
                type: matchingModel.type,
                name: matchingModel.name,
                typeParameters: matchingModel.typeParameters,
                options: matchingModel.options.map(type => expandTypeParameters(type, parameterValues))
            }
        case RpcModelKind.inline:
            return {
                type: matchingModel.type,
                typeParameters: matchingModel.typeParameters,
                name: matchingModel.name,
                inlinedType: expandTypeParameters(matchingModel.inlinedType, parameterValues)
            }
    }
}

function expandTypeParameters(type: RpcType, parameterValues: Record<string, RpcType>): RpcType {
    if (type.isTypeParameter) return parameterValues[type.name] ?? type
    return {
        name: type.name,
        // When type is a type parameter we return early
        isTypeParameter: false,
        typeArguments: type.typeArguments.map(type => expandTypeParameters(type, parameterValues)),
        inlinedType: type.inlinedType === undefined ? undefined : expandTypeParameters(type.inlinedType, parameterValues),
        isNullable: type.isNullable
    }
}


/**
 * When we have a union like this:
 * ```
 * type MyUnion = Object1 | Object2
 * ```
 * As the model, we need to determine from the actual object if it's Object1 or Object2.
 */
function resolveActualMemberOfUnionType(obj: object, model: RpcUnionModel): RpcType {
    // Note that members of union types are expected to have the type discriminator always
    if (RpcTypeDiscriminator in obj) {
        // Determine what this object is based on the "type" field
        const typeValue = obj[RpcTypeDiscriminator]
        if (typeof typeValue !== "string") {
            throw new Error(
                `Union type object 'type' discriminator is of incorrect type, it should be a string but it's a ${typeof typeValue}.` +
                `Object: ${JSON.stringify(obj)}. Model: ${JSON.stringify(model)}`
            )
        }
        // Ignore package name
        const simpleTypeName = removeBeforeLastExclusive(typeValue, ".")
        // Resolve the full type info of the model, most importantly the type arguments, by inspecting the union type.
        const modelType = model.options.find(type => simpleModelName(type.name) === simpleTypeName)
        if (modelType === undefined) {
            throw new Error(`Union type object specified its type as '${typeValue}',` +
                " but that's not one of the members of the union type it's supposed to be." +
                ` Available members: ${model.options.map(type => type.name)}` +
                ` Object: ${JSON.stringify(obj)}. Model: ${JSON.stringify(model)}`
            )
        }
        return modelType
    } else {
        throw new Error(
            "Union type object is missing 'type' discriminator and therefore its value can't " +
            `be inferred: ${JSON.stringify(obj)}. Model: ${JSON.stringify(model)}`
        )
    }
}