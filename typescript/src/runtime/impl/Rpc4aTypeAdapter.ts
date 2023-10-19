import {
    RpcModel,
    RpcModelKind,
    RpcStructModel,
    RpcType,
    RpcTypeDiscriminator,
    RpcTypeNames,
    RpcUnionModel
} from "../ApiDefinition";
import {buildRecord, objectForEach, objectMapValues, removeBeforeLastExclusive} from "./Util";


/**
 * Allow transforming any value while knowing its type.
 * Return undefined to use the original value.
 * @param key If the value is a property of an object, will evaluate to the property name.
 * @param parentType If the value is a property of an object, will evaluate to the type of that object.
 * @param polymorphic Whether a 'type' field is required because the parent of this is a member of a union type (also true when the type itself is part of a union type)
 */
type TypedValueAdapter = (key: string | undefined,
                          value: unknown,
                          type: RpcType,
                          matchingModel: RpcModel | undefined,
                          parentType: RpcType | undefined,
                          polymorphic: boolean) => unknown


const DefaultJavascriptToSpecAdapter: TypedValueAdapter = (key, value, type,
                                                           _, parentType, polymorphic) => {
    // The rpc4k spec defines all "void" typed values to be equal to "void"
    if (type.name === RpcTypeNames.Void) return "void"
    if (parentType !== undefined && key === RpcTypeDiscriminator) {
        if (polymorphic) {
            // Currently, because kotlin is being a bitch, we need to provide te package name in addition to the class name for the type discriminator.
            return parentType.packageName + "." + parentType.name
        } else {
            // "type" field is not allowed when not in the context of union types.
            // Return undefined to eliminate the "type" field.
            return undefined
        }

    }

    return value
}
const DefaultSpecToJavascriptAdapter: TypedValueAdapter = (key, value,
                                                           type, matchingModel,
                                                           parentType, polymorphic) => {
    // Typescript defines all values typed "void" will be either undefined or null.
    if (type.name === RpcTypeNames.Void) return undefined
    // Typescript still expects the simple name to be the value of the type field
    if (parentType !== undefined && key === RpcTypeDiscriminator) return parentType.name
    // Even if in this case the type is not polymorphic, we want to include the "type" field anyway because it's vital for typescript code using it.
    if (!polymorphic && matchingModel !== undefined && matchingModel.type === RpcModelKind.struct &&
        // Check if the "type" field is supposed to exist
        matchingModel.properties.some(property => property.name === RpcTypeDiscriminator)) {
        if (typeof value !== "object") throw new Error("Unexpected non-object value with a struct type")
        // Add the "type" because the server might omit it
        return {...value, type: type.name}
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

    alignJsItemWithRpcSpec(item: unknown, type: RpcType): unknown {
        return this.alignWithType(item, type, DefaultJavascriptToSpecAdapter)
    }

    alignRpcSpecItemWithJs(item: unknown, type: RpcType): unknown {
        return this.alignWithType(item, type, DefaultSpecToJavascriptAdapter)
    }

    /**
     * @param propertyName In case the item is a value of some property, propertyName should specify what the property key is.
     * @param parentType In case the item is a value of some property in some object, this will be the type of that object
     * @param polymorphic True if either this type is part of union type, or it is inside a struct model that is part of a union type.
     */
    private alignWithType(originalItem: unknown,
                          type: RpcType,
                          adapter: TypedValueAdapter,
                          propertyName?: string,
                          parentType?: RpcType,
                          polymorphic?: boolean): unknown {
        const isPolymorphic = polymorphic === true
        // matchingModel is not relevant when the originalItem is not an object
        const matchingModel = typeof originalItem === "object" ? this.getModelOfType(type) : undefined
        const item = adapter(propertyName, originalItem, type, matchingModel, parentType, isPolymorphic)
        if (item === null) return null
        if (typeof item === "object") {
            if (matchingModel !== undefined) {
                // This item is one of the models
                switch (matchingModel.type) {
                    case RpcModelKind.enum:
                        throw new Error(`Item ${JSON.stringify(item)} is invalid: it's an object but it's defined as an enum (string union)`)
                    case RpcModelKind.struct:
                        return this.alignStructWithModel(item, matchingModel, adapter, type, isPolymorphic)
                    case RpcModelKind.union: {
                        // Need to do a bit of extra work with union type to know what the actual type is in this case
                        const actualType = this.resolveActualMemberOfUnionType(item, matchingModel)
                        // Important: Specify polymorphic as true
                        return this.alignWithType(item, actualType, adapter, undefined, undefined, true)
                    }
                    //
                    // // Need to do a bit of extra work with union type to know what the actual type is in this case
                    // return this.alignStructWithModel(item, this.resolveActualMemberOfUnionType(item, matchingModel), adapter, type)
                }
            } else {
                if (Array.isArray(item)) {
                    // This item is an array or a tuple
                    if (type.name === RpcTypeNames.Tuple) {
                        return this.alignTupleWithTypeArguments(item, type, adapter);
                    } else if (type.name === RpcTypeNames.Arr) {
                        return this.alignArrayWithTypeArgument(item, type, adapter)
                    } else {
                        throw new Error(`Item is invalid: it's an array but not it's defined as such,` +
                            ` it's actually defined as a ${type.name}. Item: ${JSON.stringify(item)}`)
                    }
                } else {
                    return this.alignRecordWithKeyValues(item, type, adapter)
                }
            }
        } else {
            return item
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
        return this.expandModelTypeParameters(matchingModel, parameterValues);
    }

    private expandModelTypeParameters(matchingModel: RpcModel, parameterValues: Record<string, RpcType>): RpcModel {
        switch (matchingModel.type) {
            case RpcModelKind.enum:
                return matchingModel
            case RpcModelKind.struct:
                return {
                    type: matchingModel.type,
                    typeParameters: matchingModel.typeParameters,
                    name: matchingModel.name,
                    properties: matchingModel.properties.map(property => {
                        return {
                            name: property.name,
                            isOptional: property.isOptional,
                            type: this.expandTypeParameters(property.type, parameterValues)
                        }
                    })
                }
            case RpcModelKind.union:
                return {
                    type: matchingModel.type,
                    name: matchingModel.name,
                    typeParameters: matchingModel.typeParameters,
                    options: matchingModel.options.map(type => this.expandTypeParameters(type, parameterValues))
                }

        }
    }

    private expandTypeParameters(type: RpcType, parameterValues: Record<string, RpcType>): RpcType {
        if (type.isTypeParameter) return parameterValues[type.name] ?? type
        return {
            name: type.name,
            packageName: type.packageName,
            // When type is a type parameter we return early
            isTypeParameter: false,
            typeArguments: type.typeArguments.map(type => this.expandTypeParameters(type, parameterValues)),
            inlinedType: type.inlinedType === undefined ? undefined : this.expandTypeParameters(type.inlinedType, parameterValues),
            isNullable: type.isNullable
        }
    }

    private alignTupleWithTypeArguments(item: unknown[], type: RpcType, adapter: TypedValueAdapter) {
        if (item.length !== type.typeArguments.length) {
            throw new Error(`Tuple is invalid, it's supposed to have ${type.typeArguments.length}` +
                ` elements, but it actually has ${item.length} elements. Tuple: ${JSON.stringify(item)}`)
        }
        return item.map((element, i) => {
            const elementType = type.typeArguments[i]
            return this.alignWithType(element, elementType, adapter)
        })
    }

    private alignArrayWithTypeArgument(item: unknown[], type: RpcType, adapter: TypedValueAdapter) {
        // The RpcTypes are always trustworthy, we don't validate them. We know it has exactly one element.
        const elementType = type.typeArguments[0]
        return item.map(element => this.alignWithType(element, elementType, adapter))
    }

    /**
     * When we have a union like this:
     * ```
     * type MyUnion = Object1 | Object2
     * ```
     * As the model, we need to determine from the actual object if it's Object1 or Object2.
     */
    private resolveActualMemberOfUnionType(obj: object, model: RpcUnionModel): RpcType {
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
            const modelType = model.options.find(type => type.name === simpleTypeName)
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


    private alignStructWithModel(obj: object, model: RpcStructModel, adapter: TypedValueAdapter, objType: RpcType, polymorphic: boolean): object {
        if (Array.isArray(obj)) {
            throw new Error(`Item ${JSON.stringify(obj)} is invalid: it's an array but it's defined as a struct (object)`)
        }
        return objectMapValues(obj, (key, value) => {
            // SLOW: O(n) search on every object property
            const property = model.properties.find(property => property.name === key)
            if (property === undefined) throw new Error(`Item ${JSON.stringify(obj)} is invalid: it doesn't exist in the defined struct (interface): ${JSON.stringify(model)}`)
            // Important: pass the key of the property to the alignWithType and the type of the parent object
            return this.alignWithType(value, property.type, adapter, key, objType, polymorphic)
        })
    }

    private alignRecordWithKeyValues(obj: object, type: RpcType, adapter: TypedValueAdapter): object {
        if (type.name !== RpcTypeNames.Rec) {
            throw new Error(`Item is invalid: it's an object type but it's expected type of '${type.name}'` +
                ` Is not a defined object or array type. Item: ${JSON.stringify(obj)}`)
        }
        // The RpcTypes are always trustworthy, we don't validate them. We know it has exactly two elements.
        const [keyType, valueType] = type.typeArguments
        const resultObject: Partial<Record<string | number, unknown>> = {}

        objectForEach(obj, (key, value) => {
            //TODO: this might fail sometimes with complex keys, i'm not sure

            // Align both keys and values
            const alignedKey = this.alignWithType(key, keyType, adapter) as string | number
            resultObject[alignedKey] = this.alignWithType(value, valueType, adapter)
        })

        return resultObject
    }

}

type X = { key: string } | number[] | string[]




