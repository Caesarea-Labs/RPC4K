export namespace GeneratedCodeUtils {
    export function request<T>(client: RpcClient, format: SerializationFormat, method: string, ...args: unknown[]): T {
        const res = client.send(new Rpc(method, args))
        return format.decode(res)
    }
}