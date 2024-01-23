@file:Suppress("UNCHECKED_CAST")

package com.caesarealabs.rpc4k.runtime.api

import com.caesarealabs.rpc4k.runtime.api.components.JsonFormat
import com.caesarealabs.rpc4k.runtime.api.components.KtorManagedRpcServer
import com.caesarealabs.rpc4k.runtime.implementation.Rpc4K
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
    public val eventManager: EventManager<*>
}

public data class ServerConfig(val router: RpcRouter<*>, private val config: HandlerConfig<*>) : HandlerConfig<Any?> by config


/**
 * Small hack to get the handler and invoker to reference each other.
 */
internal class HandlerConfigImpl<out T, I>(
    handler: (I) -> T,
    invoker: (HandlerConfigImpl<T, I>) -> I,
    override val format: SerializationFormat,
    override val eventManager: EventManager<*>
) : HandlerConfig<T> {
    public val invoker: I = invoker(this)
    override val handler: T = handler(this.invoker)
}

public fun <S, I> Rpc4kIndex<S, *, I>.startServer(
    format: SerializationFormat = JsonFormat(),
    engine: RpcServerEngine.MultiCall = KtorManagedRpcServer(),
    wait: Boolean = true,
    service: (I) -> S,
) {
    createServer(format, engine, service).start(wait)
}

public fun <S, I> Rpc4kIndex<S, *, I>.createServer(
    format: SerializationFormat = JsonFormat(),
    engine: RpcServerEngine.MultiCall = KtorManagedRpcServer(),
    service: (I) -> S
): RpcServerEngine.MultiCall.Instance {
    val config = createHandlerConfig(format, engine, service)
    val serverConfig = ServerConfig(router, config)
    return engine.create(serverConfig)
}

internal fun <S, I> Rpc4kIndex<S, *, I>.createHandlerConfig(
    format: SerializationFormat,
    engine: RpcServerEngine,
    service: (I) -> S
): HandlerConfigImpl<S, I> {
    @Suppress("RemoveExplicitTypeArguments")
    // The type args are necessary.
    return HandlerConfigImpl<S, I>({ service(it) }, { createInvoker(it) }, format, engine.eventManager)
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

public suspend fun <I, O> RpcServerEngine.SingleCall.Writing<I, O>.routeRpcs(input: I, output: O, config: ServerConfig) {
    routeRpcCallImpl(input, output, config)
}

public suspend fun <I, O> RpcServerEngine.SingleCall.Returning<I, O>.routeRpcs(input: I, config: ServerConfig): O {
    return routeRpcCallImpl(input, null, config)!!
}

public suspend fun EventManager<*>.acceptEventSubscription(bytes: ByteArray, connection: EventConnection) {
    this as EventManager<EventConnection>
//    val eventManager = engine.eventManager as EventManager<EventConnection>
    println("Accept event subscription: ${bytes.decodeToString()}")
    try {
        when (val message = C2SEventMessage.fromByteArray(bytes)) {
            is C2SEventMessage.Subscribe -> subscribe(message, connection)
            is C2SEventMessage.Unsubscribe -> unsubscribe(message.event, message.listenerId)
        }
    } catch (e: InvalidRpcRequestException) {
        Rpc4K.Logger.warn("Invalid client event message", e)
        // RpcServerException messages are trustworthy
        connection.send(S2CEventMessage.SubscriptionError("Invalid client event message: ${e.message}").toByteArray())
    } catch (e: Throwable) {
        Rpc4K.Logger.error("Failed to handle request", e)
        connection.send(S2CEventMessage.SubscriptionError("Server failed to process subscription").toByteArray())
    }
}

