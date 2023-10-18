import {RpcClient} from "../RpcClient";
import {Rpc} from "../Rpc";
import {SerializationFormat} from "../SerializationFormat";
import {RpcFetchError, RpcResponseError} from "../RpcClientError";


export class FetchRpcClient implements RpcClient {
    private readonly url: string;

    constructor(url: string) {
        this.url = url
    }

    async send(rpc: Rpc, format: SerializationFormat): Promise<Uint8Array> {
        const data = rpc.toByteArray(format);
        let response: Response
        try {
            response = await fetch(this.url, {body: data, method: "POST"});
        } catch (e) {
            throw new RpcFetchError(`Fetch failed`, rpc, format, this, e as TypeError)
        }

        const exception = async (message: string): Promise<never> => {
            const body = await response.text()
            throw new RpcResponseError(message + ": " + body, rpc, format, this, body, response.status);
        };


        switch (response.status) {
            case 200:
                return new Uint8Array(await response.arrayBuffer())
            case 400:
                return await exception("Request was not valid. The client may not be up to date");
            case 404:
                return await exception(`Could not find the server at url '${this.url}'.`);
            case 500:
                return await exception("The server crashed handling the request");
            default:
                return await exception(`The server returned an unexpected status code: ${response.status}.`);
        }
    }
}

