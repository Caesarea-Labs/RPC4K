package com.caesarealabs.rpc4k.runtime.jvm.user.testing

import com.caesarealabs.rpc4k.runtime.api.*
import com.caesarealabs.rpc4k.runtime.api.components.JsonFormat
import com.caesarealabs.rpc4k.runtime.api.components.MemoryEventManager
import com.caesarealabs.rpc4k.runtime.jvm.api.PortPool
import com.caesarealabs.rpc4k.runtime.implementation.createHandlerConfig
import com.caesarealabs.rpc4k.runtime.jvm.api.KtorManagedRpcServer
import com.caesarealabs.rpc4k.runtime.jvm.api.OkHttpRpcClientFactory
import com.caesarealabs.rpc4k.runtime.user.Rpc4kIndex
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.ExtensionContext


public fun <S, C, I> Rpc4kIndex<S, C, I>.junit(
    port: Int = PortPool.get(),
    format: SerializationFormat = JsonFormat(),
    server: RpcServerEngine.MultiCall = KtorManagedRpcServer(port = port),
    client: RpcClientFactory = OkHttpRpcClientFactory(),
    eventManager: EventManager = MemoryEventManager(),
    serverFactory: (I) -> S,
): ClientServerExtension<S, C, I> {
    val url = "http://localhost:${port}"
    val websocketUrl = "$url/events"
    val clientSetup = client.build(url, websocketUrl)
    val config = createHandlerConfig(format, eventManager, server, serverFactory)
    val serverConfig = ServerConfig(router, config)
    val suite = Rpc4kSuiteImpl(config.handler, createNetworkClient(clientSetup, format), /*createMemoryClient(config.handler),*/ config.invoker)
    val engine = server.create(serverConfig)
    return ClientServerExtension(engine, port, suite)
}

public interface Rpc4kSuite<Server, Client, Invoker> {
    public val server: Server
    public val networkClient: Client

    //    public val memoryClient: Client,
    public val invoker: Invoker
}

public data class Rpc4kSuiteImpl<S, C, I>(override val server: S, override val networkClient: C, override val invoker: I) :
    Rpc4kSuite<S, C, I>


public class ClientServerExtension<S, C, I> internal constructor(
    private val engine: RpcServerEngine.MultiCall.Instance,
    public val port: Int,
    suite: Rpc4kSuite<S, C, I>
) : Extension, BeforeAllCallback,
    AfterAllCallback {
    public val server: S = suite.server
    public val client: C = suite.networkClient
    public val invoker: I = suite.invoker

    override fun beforeAll(context: ExtensionContext) {
        engine.start(wait = false)
    }

    override fun afterAll(context: ExtensionContext) {
        engine.stop()
        if (server is AutoCloseable) server.close()
    }
}


