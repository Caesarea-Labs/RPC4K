import {RpcClient} from "../RpcClient";
import {SerializationFormat} from "../SerializationFormat";
import {Rpc} from "./Rpc";
import {Rpc4aTypeAdapter} from "./Rpc4aTypeAdapter";
import {fillDefaultModelValues} from "../../../generator/src/ApiDefinitionsDefaults";
import {PartialRpcType, RpcModel} from "./ApiDefinition";
import {createRpcType, RpcTypes} from "./RpcTypeUtils";


export namespace GeneratedCodeUtils {
    export async function request<T>(
        client: RpcClient, format: SerializationFormat, adapter: Rpc4aTypeAdapter,
        method: string, args: unknown[], argTypes: PartialRpcType[], returnType?: PartialRpcType
    ): Promise<T> {
        // Adapt args to fit to the rpc spec
        const adaptedArgs = args.map((arg, i) => adapter.alignJsItemWithRpcSpec(arg, createRpcType(argTypes[i])))
        const res = await client.send(new Rpc(method, adaptedArgs), format)
        const specObject = format.decode<T>(res)
        const actualReturnType = returnType === undefined ? RpcTypes.Void : createRpcType(returnType)
        // Adapt return type to fit to javascript
        return adapter.alignRpcSpecItemWithJs(specObject, actualReturnType) as T
    }

    export function createTypeAdapter(modelJson: string): Rpc4aTypeAdapter {
        const models: RpcModel[] = JSON.parse(modelJson)
        return new Rpc4aTypeAdapter(models.map(fillDefaultModelValues))
    }
}