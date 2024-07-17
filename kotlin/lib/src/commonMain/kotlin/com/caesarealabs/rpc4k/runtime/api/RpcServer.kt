package com.caesarealabs.rpc4k.runtime.api

import com.caesarealabs.rpc4k.runtime.api.components.JsonFormat
import com.caesarealabs.rpc4k.runtime.api.components.MemoryEventManager
import com.caesarealabs.rpc4k.runtime.implementation.createHandlerConfig
import com.caesarealabs.rpc4k.runtime.user.Rpc4kIndex


/**
 * Specifies how RPC event messages should be sent
 */
public interface RpcMessageLauncher {
    /**
     * Returns true if the message was reached.
     *
     * Return false if the target of the connection is gone, and the connection should be removed
     */
    public suspend fun send(connection: EventConnection, bytes: ByteArray): Boolean
}

/**
 * A server platform that handles routing RPC requests itself, using the specified config in [start].
 * Once the server is not needed, [stop] should be called.
 */
public interface DedicatedServer: RpcMessageLauncher {
    public fun start(config: ServerConfig, wait: Boolean = true)
    public fun stop()
}

/**
 * If the condition is not true, the current RPC call will return and error signaling that there is some problem with the input of the client.
 *
 * **SECURITY NOTE** - the message will be sent to clients. Make sure to not leak sensitive info.
 * Use this to verify inputs in service methods.
 */
public inline fun serverRequirement(condition: Boolean, message: () -> String) {
    if (!condition) {
        throw InvalidRpcRequestException(message())
    }
}

public enum class RpcError {
    InvalidRequest,
    InternalError
}

//public data class ConfiguredDedicatedServer(
//    val server: DedicatedServer,
//    val config: ServerConfig
//) {
//    public fun start(wait: Boolean = true) {
//        server.start(config, wait)
//    }
//    public fun stop() {
//
//    }
//}
//
//public fun <S, I> Rpc4kIndex<S, *, I>.dedicatedServer(
//    port: Int = PortPool.get(),
//    format: SerializationFormat = JsonFormat(),
//    server: (port: Int) -> DedicatedServer = { KtorManagedRpcServer(port = it) },
//    eventManager: EventManager = MemoryEventManager(),
//    service: (I) -> S,
//): DedicatedServer {
//    val serverInstance = server(port)
////    val clientSetup = client.build(url, websocketUrl)
//    val config = createHandlerConfig(format, eventManager, serverInstance, service)
//    val serverConfig = ServerConfig(router, config)
////    val suite = Rpc4kSCServerSuite(, createNetworkClient(clientSetup, format), /*createMemoryClient(config.handler),*/ config.invoker)
//    return ClientServerExtension(
//        serverInstance,
//        serverConfig,
//        port,
//        config.handler,
//        config.invoker
//    )
//}