import {Rpc} from "./impl/Rpc";
import {SerializationFormat} from "./SerializationFormat";
import duration from 'dayjs/plugin/duration'

export interface RpcClient {
    send(rpc: Rpc, format: SerializationFormat): Promise<Uint8Array>
}