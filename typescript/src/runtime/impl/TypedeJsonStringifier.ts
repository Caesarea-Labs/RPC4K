import {RpcModel, RpcType} from "../../generator/ApiDefinition";
import {objectForEach, objectMap} from "../../generator/Utils";

//TODO: the codegen should also generate a json that has the RPCModel values so it can be used in runtime
/**
 * Sometimes the values Stringify.json gives aren't quite write, like dates have special handling and void has special handling
 * this takes care to adapt the values to their correct types, according to the model specified by the server.
 */
class Rpc4AllValueAdapter {
    private readonly modelMap: Record<string, RpcModel>

    constructor(models: RpcModel[]) {
        this.modelMap = {}
        for (const model of models) {
            this.modelMap[model.name] = model
        }
    }

    alignWithType(item: unknown, type: RpcType): unknown {
        // The rpc4k spec defines all "void" typed values to be equal to "void"
        if (type.name === "void") return "void"
        if (item === null) return null
        if (typeof item === "object") {
            const matchingModel = this.modelMap[type.name]
            if (matchingModel !== undefined) {
                // This item is one of the models
                switch (matchingModel.type) {
                    //TODO: i'm not sure if it's correct to not have array models
                    case RpcModel.Type.enum:
                        throw new Error(`Item ${JSON.stringify(item)} is invalid: it's an object but it's defined as an enum (string union)`)
                    case RpcModel.Type.struct:
                        return this.alignStructWithModel(item, matchingModel)
                    case RpcModel.Type.union:
                        // Need to do a bit of extra work with union type to know what the actual type is in this case
                        return this.alignStructWithModel(item, this.resolveActualMemberOfUnionType(item, matchingModel))
                }
            } else {
                //TODO: share constants with resolveBuiltinType, stop with the magic strings
                if (Array.isArray(item)) {
                    // This item is an array or a tuple
                    if (type.name === "tuple") {
                        return this.alignTupleWithTypeArguments(item, type);
                    } else if (type.name === "array") {
                        return this.alignArrayWithTypeArgument(item, type)
                    } else {
                        throw new Error(`Item is invalid: it's an array but not it's defined as such,` +
                            ` it's actually defined as a ${type.name}. Item: ${JSON.stringify(item)}`)
                    }
                } else {
                    return this.alignGenericObjectWithKeyValues(item, type)
                }
            }
        } else {
            return item
        }
    }

    private alignTupleWithTypeArguments(item: unknown[], type: RpcType) {
        if (item.length !== type.typeArguments.length) {
            throw new Error(`Tuple is invalid, it's supposed to have ${type.typeArguments.length}` +
                ` elements, but it actually has ${item.length} elements. Tuple: ${JSON.stringify(item)}`)
        }
        return item.map((element, i) => {
            const elementType = type.typeArguments[i]
            return this.alignWithType(element, elementType)
        })
    }

    private alignArrayWithTypeArgument(item: unknown[], type: RpcType) {
        // The RpcTypes are always trustworthy, we don't validate them. We know it has exactly one element.
        const elementType = type.typeArguments[0]
        return item.map(element => this.alignWithType(element, elementType))
    }

    /**
     * When we have a union like this:
     * ```
     * type MyUnion = Object1 | Object2
     * ```
     * As the model, we need to determine from the actual object if it's Object1 or Object2.
     */
    private resolveActualMemberOfUnionType(obj: object, model: RpcModel.Union): RpcModel.Struct {
        if ("type" in obj) {
            // Determine what this object is based on the "type" field
            const typeValue = obj["type"]
            if (typeof typeValue !== "string") {
                throw new Error(
                    `Union type object 'type' discriminator is of incorrect type, it should be a string but it's a ${typeof typeValue}.` +
                    `Object: ${JSON.stringify(obj)}. Model: ${JSON.stringify(model)}`
                )
            }
            const actualModel = this.modelMap[typeValue]
            if (actualModel === undefined) {
                throw new Error(
                    `Union type object specified its type as '${typeValue}', but no such model exists.` +
                    `Object: ${JSON.stringify(obj)}. Model: ${JSON.stringify(model)}`
                )
            }
            if (actualModel.type !== RpcModel.Type.struct) {
                throw new Error(
                    `Union type object specified its type as '${typeValue}', but that's not an actual struct (interface).` +
                    `Object: ${JSON.stringify(obj)}. Model: ${JSON.stringify(model)}`
                )
            }
            return actualModel
        } else {
            throw new Error(
                "Union type object is missing 'type' discriminator and therefore its value can't " +
                `be inferred: ${JSON.stringify(obj)}. Model: ${JSON.stringify(model)}`
            )
        }
    }

    private alignStructWithModel(obj: object, model: RpcModel.Struct): object {
        if (Array.isArray(obj)) {
            throw new Error(`Item ${JSON.stringify(obj)} is invalid: it's an array but it's defined as a struct (object)`)
        }
        return objectMap(obj, (key, value) => {
            const type = model.properties[key]
            this.alignWithType(value, type)
        })
    }

    private alignGenericObjectWithKeyValues(obj: object, type: RpcType): object {
        if (type.name !== "record") {
            throw new Error(`Item is invalid: it's an object type but it's expected type of '${type.name}'` +
                ` Is not a defined object or array type. Item: ${JSON.stringify(obj)}`)
        }
        // The RpcTypes are always trustworthy, we don't validate them. We know it has exactly two elements.
        const [keyType, valueType] = type.typeArguments
        const resultObject: Partial<Record<string | number, unknown>> = {}

        objectForEach(obj, (key, value) => {
            //TODO: this might fail sometimes with complex keys, i'm not sure

            // Align both keys and values
            const alignedKey = this.alignWithType(key, keyType) as string | number
            resultObject[alignedKey] = this.alignWithType(value, valueType)
        })

        return resultObject
    }

}

type X = { key: string } | number[] | string[]




