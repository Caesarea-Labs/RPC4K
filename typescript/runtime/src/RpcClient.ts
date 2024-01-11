import {Rpc} from "./impl/Rpc";
import {SerializationFormat} from "./SerializationFormat";
import duration from 'dayjs/plugin/duration'
import {TsSerializer} from "./serialization/TsSerializer";

export interface RpcClient {
    send(rpc: Rpc, format: SerializationFormat , argSerializers: TsSerializer<unknown>[]): Promise<Uint8Array>
}