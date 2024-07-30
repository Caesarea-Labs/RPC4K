package com.caesarealabs.rpc4k.runtime.api

import com.caesarealabs.logging.PrintLogging
import com.caesarealabs.rpc4k.runtime.platform.ConcurrentMutableMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

/**
 * Defines how to send messages to the server, and listen to responses.
 * Extending [AbstractEventClient] is usually good enough
 */
public interface EventClient {
    /**
     * Sends arbitrary information to the event accepting server. This is used to set up subscription and unsubscription.
     */
    public suspend fun send(message: ByteArray)

    /**
     * Creates a _cold_ `Flow` that listens to new events
     */
    public fun createFlow(subscribeMessage: ByteArray, unsubscribeMessage: ByteArray, listenerId: String): Flow<ByteArray>
}

/**
 * Utility that handles most of the implementation of a usual [EventClient]
 */
public abstract class AbstractEventClient: EventClient {
    /**
     * Used in conjunction with [EventClient.createFlowUtil] to manage subscription flows.
     * Usually, Values are pushed into the flows in the websocket implementation.
     */
    private val activeFlows: MutableMap<String, (ByteArray) -> Unit> = ConcurrentMutableMap()

    internal fun handleMessage(message: S2CEventMessage) {
        when (message) {
            is S2CEventMessage.Emitted -> {
                val listener = activeFlows[message.listenerId]
                if (listener != null) {
                    listener(message.payload)
                } else {
                    PrintLogging.logWarn { "Could not find listener for id '${message.listenerId}', the subscription may still open on the server" }
                }
            }

            is S2CEventMessage.SubscriptionError -> {
                error("Failed to subscribe to event: ${message.error}")
            }
        }
    }

    final override fun createFlow(subscribeMessage: ByteArray, unsubscribeMessage: ByteArray, listenerId: String): Flow<ByteArray> {
        return callbackFlow {
            // Register event for self
            activeFlows[listenerId] = {
                trySend(it)
                    .onFailure { e ->
                        if(e is CancellationException) {
                            cancel(e)
                        } else {
                            println("Failed to update event listener: $e")
                            if (e != null) throw e
                        }
                    }
            }
            // Tell the server to start sending events
            this@AbstractEventClient.send(subscribeMessage)
            awaitClose {
                launch {
                    // Tell the server to stop sending events
                    this@AbstractEventClient.send(unsubscribeMessage)
                    // Remove event reference from self
                    activeFlows.remove(listenerId)
                }
            }
        }
    }
}



