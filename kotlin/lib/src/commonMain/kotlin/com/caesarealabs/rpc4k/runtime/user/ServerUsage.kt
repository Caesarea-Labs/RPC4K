package com.caesarealabs.rpc4k.runtime.user

import com.caesarealabs.rpc4k.runtime.api.*
import com.caesarealabs.rpc4k.runtime.api.components.JsonFormat
import com.caesarealabs.rpc4k.runtime.api.components.MemoryEventManager
import com.caesarealabs.rpc4k.runtime.implementation.createHandlerConfig


public fun <S, I> Rpc4kIndex<S, *, I>.setupServer(
    //TODO: MP default
    messageLauncher: RpcMessageLauncher,
    format: SerializationFormat = JsonFormat(),
    eventManager: EventManager = MemoryEventManager(),
    service: (I) -> S
): Rpc4kSCServerSuite<S, I> {
    val config = createHandlerConfig(format, eventManager, messageLauncher, service)
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
    wait: Boolean = true, service: (I) -> S
) {
    val suite = rpc.setupServer(this, format = format, eventManager, service)
    start(suite.config, wait)
}
