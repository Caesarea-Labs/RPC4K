package com.caesarealabs.rpc4k.runtime.user

import com.caesarealabs.rpc4k.runtime.api.HandlerConfig
import com.caesarealabs.rpc4k.runtime.api.RpcClient
import com.caesarealabs.rpc4k.runtime.api.RpcRouter
import com.caesarealabs.rpc4k.runtime.api.SerializationFormat

public interface Rpc4kIndex<Server, Client, Invoker> {
    public val createNetworkClient: (rpcClient: RpcClient, format: SerializationFormat) -> Client
    // LOWPRIO: Improve server testing with "in-memory-server" client generation
 //    public val createMemoryClient: (server: Server) -> Client
    public val createInvoker: (HandlerConfig<Server>) -> Invoker
    public val router: RpcRouter<Server>
}


