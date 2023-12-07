import {RpcClient} from "../RpcClient";
import {SerializationFormat} from "../SerializationFormat";
import {Rpc} from "./Rpc";
import {Rpc4aTypeAdapter} from "./Rpc4aTypeAdapter";
import {fillDefaultModelValues} from "../../../generator/src/ApiDefinitionsDefaults";
import {RpcModel} from "./ApiDefinition";
import {TsSerializer} from "../serialization/TsSerializer";


export namespace GeneratedCodeUtils {
    export async function request<T>(
        client: RpcClient, format: SerializationFormat,
        method: string, args: unknown[], argSerializers: TsSerializer<any>[], returnType: TsSerializer<T>
    ): Promise<T> {
        // Adapt args to fit to the rpc spec
        // const adaptedArgs = args.map((arg, i) => adapter.alignJsItemWithRpcSpec(arg, createRpcType(argTypes[i])))
        const res = await client.send(new Rpc(method, args), format, argSerializers)
        return format.decode<T>(returnType, res)
        // const actualReturnType = returnType === undefined ? RpcTypes.Void : createRpcType(returnType)
        // // Adapt return type to fit to javascript
        // return adapter.alignRpcSpecItemWithJs(specObject, actualReturnType) as T
    }

    export async function send(
        client: RpcClient, format: SerializationFormat,
        method: string, args: unknown[], argSerializers: TsSerializer<any>[],
    ): Promise<void> {
        await client.send(new Rpc(method, args), format, argSerializers)
    }

    export function createTypeAdapter(modelJson: string): Rpc4aTypeAdapter {
        const models: RpcModel[] = JSON.parse(modelJson)
        return new Rpc4aTypeAdapter(models.map(fillDefaultModelValues))
    }
}