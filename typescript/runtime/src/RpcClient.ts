import {Rpc} from "./impl/Rpc";
import {SerializationFormat} from "./SerializationFormat";
import duration from 'dayjs/plugin/duration'
import {TsSerializer} from "./serialization/TsSerializer";

export interface RpcClient {
    send(rpc: Rpc, format: SerializationFormat , argSerializers: TsSerializer<unknown>[]): Promise<Uint8Array>
    events: EventClient
}

export interface EventClient {
    send(message: string): Promise<void>
    createObservable(subscribeMessage: string, unsubscribeMessage: string, listenerId: string): Observable<string>
}

export class Observable<T> {
    constructor(public observe: (callback: (newValue: T) => void) => void, public close: () => void) {
    }

    map<R>(transform: (value: T) => R): Observable<R> {
        const newObserve: (callback: (newValue: R) => void) => void = (callback) => {
            this.observe(newValue => {
                callback(transform(newValue))
            })
        }
        return new Observable<R>(newObserve, this.close)
    }
}
