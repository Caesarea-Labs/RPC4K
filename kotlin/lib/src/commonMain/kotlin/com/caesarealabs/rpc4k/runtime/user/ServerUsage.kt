package com.caesarealabs.rpc4k.runtime.user

import com.caesarealabs.logging.LoggingFactory
import com.caesarealabs.logging.PrintLoggingFactory
import com.caesarealabs.rpc4k.runtime.api.*
import com.caesarealabs.rpc4k.runtime.implementation.createHandlerConfig
import com.caesarealabs.rpc4k.runtime.user.components.JsonFormat
import com.caesarealabs.rpc4k.runtime.user.components.MemoryEventManager

/**
 * Utility for configuring an RPC server, usually used by serverless implementations.
 */
public fun <S, I> Rpc4kIndex<S, *, I>.setupServer(
    messageLauncher: RpcMessageLauncher,
    format: SerializationFormat = JsonFormat(),
    eventManager: EventManager = MemoryEventManager(),
    logging: LoggingFactory = PrintLoggingFactory,
    service: (I) -> S
): Rpc4kSCServerSuite<S, I> {
    val config = createHandlerConfig(format, eventManager, messageLauncher, logging, service)
    val serverConfig = ServerConfig(router, config)
    return Rpc4kSCServerSuite(
        serverConfig, config.handler, config.invoker
    )
}

/**
 * Will start this [DedicatedServer] and serve responses to the specified [rpc].
 */
public fun <I, S> DedicatedServer.startRpc(
    rpc: Rpc4kIndex<S, *, I>,
    format: SerializationFormat = JsonFormat(),
    eventManager: EventManager = MemoryEventManager(),
    logging: LoggingFactory = PrintLoggingFactory,
    wait: Boolean = true, service: (I) -> S
): Rpc4kSCServerSuite<S, I> {
    val suite = rpc.setupServer(this, format = format, eventManager, logging, service)
    start(suite.config, wait)
    return suite
}
