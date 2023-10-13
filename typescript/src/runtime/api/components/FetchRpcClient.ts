import {RpcClient} from "../RpcClient";
import {Rpc} from "../Rpc";
import {SerializationFormat} from "../SerializationFormat";
import {RpcResponseException} from "../RpcClientError";


export class FetchRpcClient implements RpcClient {
    private readonly url: string;

    constructor(url: string) {
        this.url = url
    }

    async send(rpc: Rpc, format: SerializationFormat): Promise<Uint8Array> {
        const data = rpc.toByteArray(format);
        const response = await fetch(this.url,{ body: data});

        const exception = async (message: string): Promise<never> => {
            throw new RpcResponseException(message, rpc, format, this, await response.text(), response.status);
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

