import {RpcClient} from "../RpcClient";
import {SerializationFormat} from "../SerializationFormat";
import {Rpc} from "../Rpc";


export namespace GeneratedCodeUtils {
    export async function request<T>(client: RpcClient, format: SerializationFormat, method: string, ...args: unknown[]): Promise<T> {
        const res = await client.send(new Rpc(method, args), format)
        return format.decode(res)
    }
}