package com.caesarealabs.rpc4k.runtime.api

import com.caesarealabs.logging.Logging
import com.caesarealabs.logging.PrintLogging
import com.caesarealabs.rpc4k.runtime.implementation.sendOrDrop
import kotlin.math.log

/**
 * Should be called by server implementations whenever a new message is received.
 * @param bytes The entire message sent by the client
 * @param connection An instance created by the server implementation that identifies the specific client that has sent the message,
 * in order to send responses back to that client.
 */
public suspend fun ServerConfig.acceptEventSubscription(bytes: ByteArray, connection: EventConnection) {
    val message = try {
        C2SEventMessage.fromByteArray(bytes)
    } catch (e: InvalidRpcRequestException) {
        // Proper logging not available yet - make do with print logging
        return invalidMessage(e, connection, PrintLogging)
    } catch (e: Throwable) {
        return serverError(e, connection, PrintLogging)
    }
    // Logging available
    return config.logging.wrapCall(message.event) {
        val logging = this@wrapCall
        try {
            // Mark what kind of event is this to make it easy to search by the specific event type
            logData("call_type") {
                when (message) {
                    is C2SEventMessage.Subscribe -> "event_sub"
                    is C2SEventMessage.Unsubscribe -> "event_unsub"
                }
            }

            when (message) {
                is C2SEventMessage.Subscribe -> config.eventManager.subscribe(message, connection)
                is C2SEventMessage.Unsubscribe -> config.eventManager.unsubscribe(message.event, message.listenerId)
            }
        } catch (e: InvalidRpcRequestException) {
            invalidMessage(e, connection, logging)
        } catch (e: Throwable) {
            serverError(e, connection, logging)
        }
    }

}

private suspend fun ServerConfig.invalidMessage(e: InvalidRpcRequestException, connection: EventConnection, logging: Logging) {
    logging.logWarn(e) { "Invalid client event message" }
    // RpcServerException messages are trustworthy
    config.sendOrDrop(connection, S2CEventMessage.SubscriptionError("Invalid client event message: ${e.message}").toByteArray(), logging)
}

private suspend fun ServerConfig.serverError(e: Throwable, connection: EventConnection, logging: Logging) {
    logging.logError(e) { "Failed to handle request" }
    config.sendOrDrop(connection, S2CEventMessage.SubscriptionError("Server failed to process subscription").toByteArray(), logging)
}
