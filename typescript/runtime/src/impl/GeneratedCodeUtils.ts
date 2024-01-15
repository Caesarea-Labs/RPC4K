import {RpcClient} from "../RpcClient"
import {SerializationFormat} from "../SerializationFormat"
import {Rpc} from "./Rpc"
import {TsSerializer} from "../serialization/TsSerializer"
import dayjs from "dayjs"
import duration from "dayjs/plugin/duration"
import {TupleSerializer} from "../serialization/BuiltinSerializers"
import {Observable} from "../Observable";

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
        method: string, args: unknown[], argSerializers: TsSerializer<unknown>[]
    ): Promise<void> {
        await client.send(new Rpc(method, args), format, argSerializers)
    }

    const textEncoder = new TextEncoder()
    const textDecoder = new TextDecoder()

    export function createObservable<T>(client: RpcClient, format: SerializationFormat, event: string, args: unknown[],
                                        argSerializers: TsSerializer<unknown>[], eventSerializer: TsSerializer<T>,
                                        watchedObjectId?: string): Observable<T> {
        const listenerId = client.events.generateUuid()
        const payload = format.encode(new TupleSerializer(argSerializers), args)
        return client.events.createObservable(
            //TODO: this probably breaks in binary formats
            // This byte -> string conversion is prob inefficient
            `sub:${event}:${listenerId}:${watchedObjectId ?? ""}:${textDecoder.decode(payload)}`,
            `unsub:${event}:${listenerId}`,
            listenerId
            //TODO: this string -> bytes conversion is also inefficient
        ).map((value) => format.decode(eventSerializer, textEncoder.encode(value)))
    }

}