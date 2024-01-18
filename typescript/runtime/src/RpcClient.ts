import {Rpc} from "./impl/Rpc";
import {SerializationFormat} from "./SerializationFormat";
import {TsSerializer} from "./serialization/TsSerializer";
import {Observable} from "./Observable";

export interface RpcClient {
    send(rpc: Rpc, format: SerializationFormat , argSerializers: TsSerializer<unknown>[]): Promise<Uint8Array>
    events: EventClient
}

export interface EventClient {
    //TODO: maybe this should be a ByteArray or something
    send(message: string): Promise<void>
    createObservable(subscribeMessage: string, unsubscribeMessage: string, listenerId: string): Observable<string>

    /**
     * Since UUID sources are different in browser and in node we need to generate a uuid differently per client
     */
    generateUuid(): string
}

