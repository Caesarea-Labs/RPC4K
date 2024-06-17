package com.caesarealabs.rpc4k.runtime.user

import com.caesarealabs.rpc4k.runtime.api.EventManager
import com.caesarealabs.rpc4k.runtime.api.Rpc4kSCServerSuite
import com.caesarealabs.rpc4k.runtime.api.RpcServerEngine
import com.caesarealabs.rpc4k.runtime.api.SerializationFormat
import com.caesarealabs.rpc4k.runtime.api.ServerConfig
import com.caesarealabs.rpc4k.runtime.api.components.JsonFormat
import com.caesarealabs.rpc4k.runtime.api.components.MemoryEventManager
import com.caesarealabs.rpc4k.runtime.api.start
import com.caesarealabs.rpc4k.runtime.implementation.createHandlerConfig


public fun <S, I> Rpc4kIndex<S, *, I>.startDedicatedServer(
    //TODO: MP default
    engine: RpcServerEngine.MultiCall /*= KtorManagedRpcServer()*/,
    format: SerializationFormat = JsonFormat(),
    eventManager: EventManager = MemoryEventManager(),
    wait: Boolean = true,
    service: (I) -> S,
) {
    createDedicatedServer(engine, format, eventManager, service).start(wait)
}

/**
 * Creates a MultiCall server
 */
public fun <S, I> Rpc4kIndex<S, *, I>.createDedicatedServer(
    //TODO: MP default
    engine: RpcServerEngine.MultiCall /*= KtorManagedRpcServer()*/,
    format: SerializationFormat = JsonFormat(),
    eventManager: EventManager = MemoryEventManager(),
    service: (I) -> S
): Rpc4kSCServerSuite<S, I, RpcServerEngine.MultiCall.Instance> {
    val config = createHandlerConfig(format, eventManager, engine, service)
    val serverConfig = ServerConfig(router, config)
    return Rpc4kSCServerSuite(
        engine.create(serverConfig), serverConfig, config.handler, config.invoker
    )
}

/**
 * Creates a SingleCall server
 */
public fun <S, Inv, E: RpcServerEngine> Rpc4kIndex<S, *, Inv>.createSingleCallServer(
    engine: E,
    format: SerializationFormat = JsonFormat(),
    eventManager: EventManager = MemoryEventManager(),
    service: (Inv) -> S
): Rpc4kSCServerSuite<S, Inv, E> {
    val config = createHandlerConfig(format, eventManager, engine, service)
    val serverConfig = ServerConfig(router, config)
    return Rpc4kSCServerSuite(
        engine, serverConfig, config.handler, config.invoker
    )
}