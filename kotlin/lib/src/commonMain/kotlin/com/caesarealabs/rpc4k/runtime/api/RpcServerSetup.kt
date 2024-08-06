
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
//public interface HandlerConfig<out T> {
//    public val handler: T
//    public val format: SerializationFormat
//    public val eventManager: EventManager
//    public val messageLauncher: RpcMessageLauncher
//    public val logging: LoggingFactory
//
//    public object InMemory : HandlerConfig<Nothing> {
//        override val handler: Nothing get() = error("No actual handler is used as everything is done in-memory")
//        override val format: SerializationFormat get() = error("No serialization is used as everything is done in-memory")
//        override val eventManager: EventManager = MemoryEventManager()
//        override val messageLauncher: RpcMessageLauncher get() = error("No Server Engine is used as everything is done in-memory")
//        override val logging: LoggingFactory = PrintLoggingFactory
//    }
//}

/**
 * All information an RPC server needs to function - the [router] and the [config] to feed to the router, and server implementations.
 */
public data class TypedServerConfig<S,I>(val router: RpcRouter<*>, val config: TypedHandlerConfig<S,I>)
public typealias ServerConfig = TypedServerConfig<*,*>


//
//
//
//public data class Rpc4kSCServerSuite<S, Inv>(
//    val config: ServerConfig,
//    val server: S,
//    val invoker: Inv
//)

