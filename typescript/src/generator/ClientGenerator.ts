import {generateModels} from "./ModelGenerator";
import * as fs from "fs";
import {generateAccessor} from "./ClientAccessorGenerator";
import {fillDefaultApiDefinitionValues} from "../runtime/impl/ApiDefinitionsDefaults";
import {ApiDefinition} from "../runtime/ApiDefinition";


export interface Rpc4TsClientGenerationOptions {
    /**
     * True only for tests in rpc4ts itself. Makes it so imports use a relative path instead of a package path.
     */
    localLibPaths: boolean
}

export function generateClientModel(definitionJson: string, writeTo: string, options: Rpc4TsClientGenerationOptions) {
    // This value doesn't contain default values
    const rawApi = JSON.parse(definitionJson) as ApiDefinition
    const api = fillDefaultApiDefinitionValues(rawApi)
    const models = generateModels(api.models)
    const accessor = generateAccessor(api, options)

    fs.mkdirSync(writeTo, {recursive: true})
    fs.writeFileSync(writeTo + `${api.name}Models.ts`, models)
    fs.writeFileSync(writeTo + `${api.name}Api.ts`, accessor)
    const runtimeModelsName = `${api.name}RuntimeModels`
    // We write out a definition without the default values because it's easy to resolve them at runtime
    fs.writeFileSync(writeTo + `${runtimeModelsName}.ts`, `export const ${runtimeModelsName} = \`${JSON.stringify(rawApi.models)}\``)
}

