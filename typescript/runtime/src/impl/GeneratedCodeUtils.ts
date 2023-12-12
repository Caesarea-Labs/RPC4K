import {RpcClient} from "../RpcClient";
import {SerializationFormat} from "../SerializationFormat";
import {Rpc} from "./Rpc";
import {TsSerializer} from "../serialization/TsSerializer";
import dayjs from "dayjs";
import duration from "dayjs/plugin/duration";
dayjs.extend(duration)


export namespace GeneratedCodeUtils {
    export async function request<T>(
        client: RpcClient, format: SerializationFormat,
        method: string, args: unknown[], argSerializers: TsSerializer<unknown>[], returnType: TsSerializer<T>
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
        method: string, args: unknown[], argSerializers: TsSerializer<unknown>[],
    ): Promise<void> {
        await client.send(new Rpc(method, args), format, argSerializers)
    }

}