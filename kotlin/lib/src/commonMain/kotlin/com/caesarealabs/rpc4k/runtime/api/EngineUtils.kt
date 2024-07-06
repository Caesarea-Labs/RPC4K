package com.caesarealabs.rpc4k.runtime.api

import com.caesarealabs.rpc4k.runtime.implementation.Rpc4kLogger
import com.caesarealabs.rpc4k.runtime.implementation.sendOrDrop

public suspend fun ServerConfig.acceptEventSubscription(bytes: ByteArray, connection: EventConnection) {
    try {
        when (val message = C2SEventMessage.fromByteArray(bytes)) {
            is C2SEventMessage.Subscribe -> eventManager.subscribe(message, connection)
            is C2SEventMessage.Unsubscribe -> eventManager.unsubscribe(message.event, message.listenerId)
        }
    } catch (e: InvalidRpcRequestException) {
        Rpc4kLogger.warn("Invalid client event message", e)
        // RpcServerException messages are trustworthy
        sendOrDrop(connection, S2CEventMessage.SubscriptionError("Invalid client event message: ${e.message}").toByteArray())
    } catch (e: Throwable) {
        Rpc4kLogger.error("Failed to handle request", e)
        sendOrDrop(connection, S2CEventMessage.SubscriptionError("Server failed to process subscription").toByteArray())
    }
}

