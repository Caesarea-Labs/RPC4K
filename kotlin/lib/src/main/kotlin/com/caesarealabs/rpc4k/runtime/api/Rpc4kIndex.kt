package com.caesarealabs.rpc4k.runtime.api

public interface Rpc4kIndex<Server, Client, Invoker> {
    public val createNetworkClient: (rpcClient: RpcClient, format: SerializationFormat) -> Client
    public val createMemoryClient: (server: Server) -> Client
    public val createInvoker: (HandlerConfig<Server>) -> Invoker
    public val serverHandler:  GeneratedServerHelper<Server>
}