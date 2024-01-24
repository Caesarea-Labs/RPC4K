@file:Suppress("UNCHECKED_CAST")

package com.caesarealabs.rpc4k.runtime.api

import com.caesarealabs.rpc4k.runtime.api.components.JsonFormat
import com.caesarealabs.rpc4k.runtime.api.components.KtorManagedRpcServer
import com.caesarealabs.rpc4k.runtime.api.components.MemoryEventManager
import com.caesarealabs.rpc4k.runtime.implementation.createHandlerConfig
import com.caesarealabs.rpc4k.runtime.implementation.routeRpcCallImpl


//public typealias AnyRpcServerSetup = RpcServerSetup<*, *, *>
//public typealias RpcSetupOf<T> = RpcServerSetup<T, *, *>
//public typealias RpcSetupEngine<E> = RpcServerSetup<*, E, *>

//public interface WithFormat {
//}

/**
 * Needs to be an interface to allow the invoker to access the handler
 */
public interface HandlerConfig<out T> {
    public val handler: T
    public val format: SerializationFormat
    public val eventManager: EventManager
    public object None: HandlerConfig<Nothing> {
        override val handler: Nothing
            get() = TODO("Not yet implemented")
        override val format: SerializationFormat
            get() = TODO("Not yet implemented")
        override val eventManager: EventManager
            get() = TODO("Not yet implemented")

    }
}

public data class ServerConfig(val router: RpcRouter<*>, private val config: HandlerConfig<*>) : HandlerConfig<Any?> by config


/**
 * Small hack to get the handler and invoker to reference each other.
 */
internal class HandlerConfigImpl<out T, I>(
    handler: (I) -> T,
    invoker: (HandlerConfigImpl<T, I>) -> I,
    override val format: SerializationFormat,
    override val eventManager: EventManager
) : HandlerConfig<T> {
    public val invoker: I = invoker(this)
    override val handler: T = handler(this.invoker)
}

public fun <S, I> Rpc4kIndex<S, *, I>.startServer(
    engine: RpcServerEngine.MultiCall = KtorManagedRpcServer(),
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
    engine: RpcServerEngine.MultiCall = KtorManagedRpcServer(),
    format: SerializationFormat = JsonFormat(),
    eventManager: EventManager = MemoryEventManager(),
    service: (I) -> S
): Rpc4kSCServerSuite<S, I, RpcServerEngine.MultiCall.Instance>  {
    val config = createHandlerConfig(format, eventManager, service)
    val serverConfig = ServerConfig(router, config)
    return Rpc4kSCServerSuite(
        engine.create(serverConfig), serverConfig, config.handler, config.invoker
    )
}

/**
 * Creates a SingleCall server
 */
public fun <S, Inv, E> Rpc4kIndex<S, *, Inv>.createServer(
    engine: E,
    format: SerializationFormat = JsonFormat(),
    eventManager: EventManager = MemoryEventManager(),
    service: (Inv) -> S
): Rpc4kSCServerSuite<S, Inv, E> {
    val config = createHandlerConfig(format, eventManager, service)
    val serverConfig = ServerConfig(router, config)
    return Rpc4kSCServerSuite(
        engine, serverConfig, config.handler, config.invoker
    )
}


public data class Rpc4kSCServerSuite<S, Inv, E>(
    val engine: E,
    val config: ServerConfig,
    val server: S,
    val invoker: Inv
)

public typealias Rpc4kWithEngine<E> = Rpc4kSCServerSuite<*, *, E>


public suspend fun <I, O, E : RpcServerEngine.SingleCall.Returning<I, O>> Rpc4kWithEngine<E>.routeRpcs(input: I): O {
    return engine.routeRpcCallImpl(input, null, config)!!
}

public suspend fun <I, O, E : RpcServerEngine.SingleCall.Writing<I, O>> Rpc4kWithEngine<E>.routeRpcs(input: I, output: O) {
    engine.routeRpcCallImpl(input, output, config)
}

public fun <E : RpcServerEngine.MultiCall.Instance> Rpc4kWithEngine<E>.start(wait: Boolean) {
    engine.start(wait)
}

public fun <E : RpcServerEngine.MultiCall.Instance> Rpc4kWithEngine<E>.stop() {
    engine.stop()
}


//internal
//    public fun <T>create(handler: (HandlerConfig<T>) -> T,
//                         format: SerializationFormat,
//                         eventManager: EventManager<*>) {
//        return
//    }
//}

//public class RpcServerSetup<T, Engine : RpcServerEngine, Invoker>(
////    internal val handlerProvider: (Invoker) -> T,
//    public val handler: T,
//    public val generated: Rpc4kIndex<T, *, Invoker>,
////    public val generatedHelper: GeneratedServerHelper<T, Invoker>,
//    public val engine: Engine,
//    public val format: SerializationFormat = JsonFormat(),
////    invokerProvider: (RpcServerSetup<T,Engine>) -> GeneratedEventInvoker<T>
////    val eventHelper: GeneratedEventHelper<T>? = null
//) {
//    public fun <New : RpcServerEngine> withEngine(engine: New): RpcServerSetup<T, New, Invoker> {
//        return RpcServerSetup(handlerProvider, generated, engine, format)
//    }
//
////    internal constructor(
////        handler: T,
////        generatedHelper: GeneratedServerHelper<T>,
////        engine: Engine,
////        format: SerializationFormat = JsonFormat()
////    ) : this(handlerProvider = { handler }, generatedHelper, engine, format)
//
////    public companion object {
////        // i want to get rid of this, not a very great api?
////        public fun <T, Invoker> managedKtor(
////            handler: (Invoker) -> T, generated: Rpc4kIndex<T, *, Invoker>, format: SerializationFormat = JsonFormat(),
////            ktorServer: KtorManagedRpcServer = KtorManagedRpcServer()
////        ): RpcServerSetup<T, KtorManagedRpcServer, Invoker> {
////            return RpcServerSetup(handler, generated, ktorServer, format)
////        }
////    }
//
////    public val invoker: Invoker = generatedHelper.createInvoker(this)
////    public val handler: T = handlerProvider(invoker)
//}

//public fun <T, OldEngine : RpcServerEngine, NewEngine : RpcServerEngine, I> RpcServerSetup<T, OldEngine, I>.withEngine(
//    engine: NewEngine
//): RpcServerSetup<T, NewEngine, I> =
//    RpcServerSetup(handler, generatedHelper, engine, format)


//public fun <Engine : RpcServerEngine.MultiCall> RpcSetupEngine<Engine>.createServer(): RpcServerEngine.MultiCall.Instance = engine.create(this)
//public fun <Engine : RpcServerEngine.MultiCall> RpcSetupEngine<Engine>.startServer(wait: Boolean): Unit = engine.create(this)
//    .start(wait)


