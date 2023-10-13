import {ApiDefinition} from "./ApiDefinition";
import {generateModels} from "./ModelGenerator";
import {generateAccessor} from "./ClientAccessorGenerator";

export function generateClientModel(definitionJson: string, toDir: string) {
    const api = JSON.parse(definitionJson) as ApiDefinition
    const models = generateModels(api.models)
    const accessor = generateAccessor(api)
    // TODO: models.writeTo(toPath + "${api.name}Models")
    // TODO: accessor.writeTo(toPath + "${api.name}Api")
}