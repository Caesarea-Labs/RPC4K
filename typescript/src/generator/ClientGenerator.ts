import {ApiDefinition} from "./ApiDefinition";
import {generateModels} from "./ModelGenerator";
import * as fs from "fs";
import {generateAccessor} from "./ClientAccessorGenerator";


export interface Rpc4TsClientGenerationOptions {
    /**
     * True only for tests in rpc4ts itself. Makes it so imports use a relative path instead of a package path.
     */
    localLibPaths: boolean
}

export function generateClientModel(definitionJson: string, toDir: string, options: Rpc4TsClientGenerationOptions) {
    const api = JSON.parse(definitionJson) as ApiDefinition
    const models = generateModels(api.models)
    const accessor = generateAccessor(api, options)
    console.log(models)

    fs.mkdirSync(toDir, {recursive: true})
    fs.writeFileSync(toDir + `${api.name}Models.ts`, models)
    fs.writeFileSync(toDir + `${api.name}Api.ts`, accessor)
    // TODO: models.writeTo(toPath + "${api.name}Models")
    // TODO: accessor.writeTo(toPath + "${api.name}Api")
}