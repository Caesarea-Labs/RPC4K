@file:Suppress("UNCHECKED_CAST")

package com.caesarealabs.rpc4k.runtime.api

import com.caesarealabs.rpc4k.runtime.api.components.JsonFormat
import com.caesarealabs.rpc4k.runtime.api.components.KtorManagedRpcServer
import com.caesarealabs.rpc4k.runtime.implementation.Rpc4K
import com.caesarealabs.rpc4k.runtime.implementation.RpcEventData
import com.caesarealabs.rpc4k.runtime.implementation.handleImpl



public typealias AnyRpcServerSetup = RpcServerSetup<*, *, *>
public typealias RpcSetupOf<T> = RpcServerSetup<T, *, *>
public typealias RpcSetupEngine<E> = RpcServerSetup<*, E, *>


public class RpcServerSetup<T, Engine : RpcServerEngine, Invoker>(
    internal val handlerProvider: (Invoker) -> T,
    public val generatedHelper: GeneratedServerHelper<T, Invoker>,
    public val engine: Engine,
    public val format: SerializationFormat = JsonFormat(),
//    invokerProvider: (RpcServerSetup<T,Engine>) -> GeneratedEventInvoker<T>
//    val eventHelper: GeneratedEventHelper<T>? = null
) {
    internal constructor(
        handler: T,
        generatedHelper: GeneratedServerHelper<T, Invoker>,
        engine: Engine,
        format: SerializationFormat = JsonFormat()
    ) : this(handlerProvider = { handler }, generatedHelper, engine, format)

    public companion object {
        // i want to get rid of this, not a very great api?
        public fun <T, Invoker> managedKtor(
            handler: (Invoker) -> T, generatedHelper: GeneratedServerHelper<T, Invoker>, format: SerializationFormat = JsonFormat(),
            ktorServer: KtorManagedRpcServer = KtorManagedRpcServer()
        ): RpcServerSetup<T, KtorManagedRpcServer, Invoker> {
            return RpcServerSetup(handler, generatedHelper, ktorServer, format)
        }
    }

    public val invoker: Invoker = generatedHelper.createInvoker(this)
    public val handler: T = handlerProvider(invoker)
}

public fun <T, OldEngine : RpcServerEngine, NewEngine : RpcServerEngine, I> RpcServerSetup<T, OldEngine, I>.withEngine(
    engine: NewEngine
): RpcServerSetup<T, NewEngine, I> =
    RpcServerSetup(handler, generatedHelper, engine, format)


public fun <Engine : RpcServerEngine.MultiCall> RpcSetupEngine<Engine>.createServer(): RpcServerEngine.MultiCall.Instance = engine.create(this)
public fun <Engine : RpcServerEngine.MultiCall> RpcSetupEngine<Engine>.startServer(wait: Boolean): Unit = engine.create(this)
    .start(wait)

public suspend fun <I, O, Engine : RpcServerEngine.SingleCall.Writing<I, O>> RpcSetupEngine<Engine>.handleRequests(input: I, output: O) {
    handleImpl(input, output)
}

public suspend fun <I, O, Engine : RpcServerEngine.SingleCall.Returning<I, O>> RpcSetupEngine<Engine>.handleRequests(input: I): O {
    return handleImpl(input, null)!!
}

public suspend fun AnyRpcServerSetup.acceptEventSubscription(bytes: ByteArray, connection: EventConnection) {
    val eventManager = engine.eventManager as EventManager<EventConnection>
    println("Accept event subscription: ${bytes.decodeToString()}")
    try {
        when (val message = EventMessage.fromByteArray(bytes)) {
            is EventMessage.Subscribe -> eventManager.subscribe(message, connection)
            is EventMessage.Unsubscribe -> eventManager.unsubscribe(message.event, message.listenerId)
        }
    } catch (e: InvalidRpcRequestException) {
        Rpc4K.Logger.warn("Invalid client event message", e)
        // RpcServerException messages are trustworthy
        connection.send(RpcEventData.SubscriptionError("Invalid client event message: ${e.message}").toByteArray())
    } catch (e: Throwable) {
        Rpc4K.Logger.error("Failed to handle request", e)
        connection.send(RpcEventData.SubscriptionError("Server failed to process subscription").toByteArray())
    }
}

