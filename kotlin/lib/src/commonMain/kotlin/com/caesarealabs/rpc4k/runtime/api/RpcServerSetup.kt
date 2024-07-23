@file:Suppress("UNCHECKED_CAST")

package com.caesarealabs.rpc4k.runtime.api

import com.caesarealabs.rpc4k.runtime.api.components.MemoryEventManager



/**
 * All configuration a generated RPC responding router needs to function.
 *
 * A _handler_ is the user class annotated with @Api.
 *
 * Needs to be an interface to allow the invoker to access the handler
 */
public interface HandlerConfig<out T> {
    public val handler: T
    public val format: SerializationFormat
    public val eventManager: EventManager
    public val messageLauncher: RpcMessageLauncher

    public object InMemory : HandlerConfig<Nothing> {
        override val handler: Nothing get() = error("No actual handler is used as everything is done in-memory")
        override val format: SerializationFormat get() = error("No serialization is used as everything is done in-memory")
        override val eventManager: EventManager = MemoryEventManager()
        override val messageLauncher: RpcMessageLauncher get() = error("No Server Engine is used as everything is done in-memory")
    }
}

public data class ServerConfig(val router: RpcRouter<*>, private val config: HandlerConfig<Any?>) : HandlerConfig<Any?> by config


/**
 * Small hack to get the handler and invoker to reference each other.
 */
internal class HandlerConfigImpl<out T, I>(
    handler: (I) -> T,
    invoker: (HandlerConfigImpl<T, I>) -> I,
    override val format: SerializationFormat,
    override val eventManager: EventManager,
    override val messageLauncher: RpcMessageLauncher
) : HandlerConfig<T> {
    val invoker: I = invoker(this)
    override val handler: T = handler(this.invoker)
}



public data class Rpc4kSCServerSuite<S, Inv>(
    val config: ServerConfig,
    val server: S,
    val invoker: Inv
)

