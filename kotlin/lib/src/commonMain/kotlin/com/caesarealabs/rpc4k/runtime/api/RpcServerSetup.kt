
package com.caesarealabs.rpc4k.runtime.api

import com.caesarealabs.logging.LoggingFactory

public typealias HandlerConfig<T>  = TypedHandlerConfig<T, *>

/**
 * All configuration a generated RPC responding router needs to function.
 *
 * A _handler_ is the user class annotated with @Api.
 *
 * Needs to be an interface to allow the invoker to access the handler
 */

public class TypedHandlerConfig<out T, I>(
    handler: (I) -> T,
    invoker: (TypedHandlerConfig<T, I>) -> I,
    public val format: SerializationFormat,
    public val eventManager: EventManager,
    public val messageLauncher: RpcMessageLauncher,
    public val logging: LoggingFactory
) {
    /**
     * Small hack to get the handler and invoker to reference each other.
     */
    public val invoker: I = invoker(this)
     public val handler: T = handler(this.invoker)
}


/**
 * All information an RPC server needs to function - the [router] and the [config] to feed to the router, and server implementations.
 */
public data class TypedServerConfig<S,I>(val router: RpcRouter<*>, val config: TypedHandlerConfig<S,I>)
public typealias ServerConfig = TypedServerConfig<*,*>




