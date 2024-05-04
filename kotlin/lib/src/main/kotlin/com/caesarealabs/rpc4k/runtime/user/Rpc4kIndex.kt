package com.caesarealabs.rpc4k.runtime.user

import com.caesarealabs.rpc4k.runtime.api.EventManager
import com.caesarealabs.rpc4k.runtime.api.HandlerConfig
import com.caesarealabs.rpc4k.runtime.api.Rpc4kSCServerSuite
import com.caesarealabs.rpc4k.runtime.api.RpcClient
import com.caesarealabs.rpc4k.runtime.api.RpcRouter
import com.caesarealabs.rpc4k.runtime.api.RpcServerEngine
import com.caesarealabs.rpc4k.runtime.api.SerializationFormat
import com.caesarealabs.rpc4k.runtime.api.ServerConfig
import com.caesarealabs.rpc4k.runtime.api.components.JsonFormat
import com.caesarealabs.rpc4k.runtime.api.components.KtorManagedRpcServer
import com.caesarealabs.rpc4k.runtime.api.components.MemoryEventManager
import com.caesarealabs.rpc4k.runtime.api.start
import com.caesarealabs.rpc4k.runtime.implementation.createHandlerConfig

public interface Rpc4kIndex<Server, Client, Invoker> {
    public val createNetworkClient: (rpcClient: RpcClient, format: SerializationFormat) -> Client
    //TODO:
//    public val createMemoryClient: (server: Server) -> Client
    public val createInvoker: (HandlerConfig<Server>) -> Invoker
    public val router: RpcRouter<Server>
}


