import {EventClient, RpcClient} from "../RpcClient"
import {Rpc} from "../impl/Rpc"
import {SerializationFormat} from "../SerializationFormat"
import {RpcFetchError, RpcResponseError} from "../RpcClientError"
import {TsSerializer} from "../serialization/TsSerializer"
import {WebsocketEventClient} from "./WebsocketEventClient"
import {GenericWebsocket} from "./GenericWebsocket"


export class FetchRpcClient implements RpcClient {
    events: EventClient;
    constructor(private readonly url: string, websocket: GenericWebsocket){
        this.events = new WebsocketEventClient(websocket)
    }

    async send(rpc: Rpc, format: SerializationFormat, argSerializers: TsSerializer<unknown>[]): Promise<Uint8Array> {
        const data = rpc.toByteArray(format, argSerializers)
        let response: Response
        try {
            response = await fetch(this.url, {body: data, method: "POST", headers: {"content-type": "application/json"}})
        } catch (e) {
            throw new RpcFetchError(`Fetch failed`, rpc, format, this, e as TypeError)
        }

        const exception = async (message: string): Promise<never> => {
            const body = await response.text()
            throw new RpcResponseError(message + ": " + body + `. Request: ${rpc.method}(${rpc.methodArgs})`, rpc, format, this, body, response.status)
        }


        switch (response.status) {
            case 200:
                return new Uint8Array(await response.arrayBuffer())
            case 400:
                return await exception("Request was not valid. The client may not be up to date.")
            case 404:
                return await exception(`Could not find the server at url '${this.url}'.`)
            case 500:
                return await exception("The server crashed handling the request")
            default:
                return await exception(`The server returned an unexpected status code: ${response.status}.`)
        }
    }
}

