package com.caesarealabs.rpc4k.runtime.implementation

import com.caesarealabs.rpc4k.runtime.api.*
import com.caesarealabs.rpc4k.runtime.api.S2CEventMessage

public suspend fun ServerConfig.acceptEventSubscription(bytes: ByteArray, connection: EventConnection) {
//    val eventManager = engine.eventManager as EventManager<EventConnection>
    println("Accept event subscription: ${bytes.decodeToString()}")
    try {
        when (val message = C2SEventMessage.fromByteArray(bytes)) {
            is C2SEventMessage.Subscribe -> eventManager.subscribe(message, connection)
            is C2SEventMessage.Unsubscribe -> eventManager.unsubscribe(message.event, message.listenerId)
        }
    } catch (e: InvalidRpcRequestException) {
        Rpc4K.Logger.warn("Invalid client event message", e)
        // RpcServerException messages are trustworthy
        sendOrDrop(connection, S2CEventMessage.SubscriptionError("Invalid client event message: ${e.message}").toByteArray())
    } catch (e: Throwable) {
        Rpc4K.Logger.error("Failed to handle request", e)
        sendOrDrop(connection, S2CEventMessage.SubscriptionError("Server failed to process subscription").toByteArray())
    }
}

public suspend fun <I, O> RpcServerEngine.SingleCall.Writing<I, O>.routeRpcs(input: I, output: O, config: ServerConfig) {
    routeRpcCallImpl(input, output, config)
}

public suspend fun <I, O> RpcServerEngine.SingleCall.Returning<I, O>.routeRpcs(input: I, config: ServerConfig): O {
    return routeRpcCallImpl(input, null, config)!!
}
