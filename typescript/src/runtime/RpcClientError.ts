import {SerializationFormat} from "./SerializationFormat";
import {Rpc} from "./Rpc";
import {RpcClient} from "./RpcClient";

/**
 * Represents any sort of error that happened as a result of an RPC call
 */
export class RpcError extends Error {
    readonly rpc: Rpc;
    readonly format: SerializationFormat;
    readonly client: RpcClient;

    constructor(
        message: string,
        rpc: Rpc,
        format: SerializationFormat,
        client: RpcClient,
    ) {
        super(message);
        this.rpc = rpc;
        this.format = format;
        this.client = client;
    }
}

/**
 * Represents that the fetch call itself failed, often meaning that no connection can be established or something like a
 * cors error
 */
export class RpcFetchError extends RpcError {
    readonly fetchError: TypeError

    constructor(
        message: string,
        rpc: Rpc,
        format: SerializationFormat,
        client: RpcClient,
        fetchError: TypeError,
    ) {
        super(message, rpc, format, client);
        this.fetchError = fetchError
    }
}

/**
 * Represents that the server has declared that it cannot return a valid response because something wrong happened
 */
export class RpcResponseError extends RpcError {
    readonly response: string;
    readonly code: number;

    constructor(
        message: string,
        rpc: Rpc,
        format: SerializationFormat,
        client: RpcClient,
        response: string,
        code: number
    ) {
        super(message, rpc, format, client);
        this.response = response;
        this.code = code;
    }
}