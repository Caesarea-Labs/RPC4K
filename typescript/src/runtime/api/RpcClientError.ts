import {SerializationFormat} from "./SerializationFormat";
import {Rpc} from "./Rpc";
import {RpcClient} from "./RpcClient";

export class RpcResponseException extends Error {
    rpc: Rpc;
    format: SerializationFormat;
    client: RpcClient;
    response: string;
    code: number;

    constructor(
        message: string,
        rpc: Rpc,
        format: SerializationFormat,
        client: RpcClient,
        response: string,
        code: number
    ) {
        super(message);
        this.rpc = rpc;
        this.format = format;
        this.client = client;
        this.response = response;
        this.code = code;
    }
}