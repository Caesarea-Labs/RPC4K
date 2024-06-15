package com.caesarealabs.rpc4k.runtime.api.components

import com.caesarealabs.rpc4k.runtime.api.*
import com.caesarealabs.rpc4k.runtime.implementation.Rpc4K
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.KSerializer
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume

public class OkHttpRpcClient(
    private val url: String, private val websocketUrl: String,

//                             private val client: OkHttpClient = OkHttpClient.Builder().readTimeout(Duration.ofSeconds(20)).build()) :
    private val client: OkHttpClient = OkHttpClient()
) :
    RpcClient {

    override suspend fun send(rpc: Rpc, format: SerializationFormat, serializers: List<KSerializer<*>>): ByteArray {
        val data = rpc.toByteArray(format, serializers)
        val response = client.request(Request(url.toHttpUrl(), body = data.toRequestBody()))
        fun exception(message: String): Nothing = throw RpcResponseException(message, rpc, format, this, response.body.string(), response.code)
        when (response.code) {
            200 -> return response.body.bytes()
            400 -> exception("Request was not valid. The client may not be up to date")
            404 -> exception("Could not find the server at url '$url'.")
            500 -> exception("The server crashed handling the request")
            else -> exception("The server returned an unexpected status code: ${response.code}.")
        }
    }

    override val events: EventClient
        get() = OkHttpWebsocketEventClient(websocketUrl, client)
}

private suspend fun OkHttpClient.request(request: Request): Response = suspendCancellableCoroutine { cont ->
    newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            cont.cancel(e)
        }

        override fun onResponse(call: Call, response: Response) {
            cont.resume(response)
        }
    })
}

private class OkHttpWebsocketEventClient(url: String, client: OkHttpClient) : EventClient {
    private val activeFlows = ConcurrentHashMap<String, (ByteArray) -> Unit>()
    val webSocket by lazy {
        client.newWebSocket(Request(url.toHttpUrl()), object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                when (val parsed = S2CEventMessage.fromString(text)) {
                    is S2CEventMessage.Emitted -> {
                        val listener = activeFlows[parsed.listenerId]
                        if (listener != null) {
                            listener(parsed.payload)
                        } else {
                            Rpc4K.Logger.warn("Could not find listener for id '${parsed.listenerId}', the subscription may still open on the server")
                        }
                    }

                    is S2CEventMessage.SubscriptionError -> {
                        error("Failed to subscribe to event: ${parsed.error}")
                    }
                }
            }
        })
    }

    override suspend fun send(message: ByteArray) {
        //TODO: kind of inefficient bytes -> string conversion
        webSocket.send(message.decodeToString())
    }

    override fun createFlow(subscribeMessage: ByteArray, unsubscribeMessage: ByteArray, listenerId: String): Flow<ByteArray> {
        return createFlow(activeFlows, subscribeMessage, unsubscribeMessage, listenerId)
    }

//    override suspend fun createFlow(subscribeMessage: ByteArray, unsubscribeMessage: ByteArray, listenerId: String): Flow<ByteArray> {
//        return callbackFlow {
//            // Register event for self
//            activeFlows[listenerId] = {
//                trySendBlocking(it)
//            }
//            // Tell the server to start sending events
//            this@OkHttpWebsocketEventClient.send(subscribeMessage)
//            awaitClose {
//                launch {
//                    // Tell the server to stop sending events
//                    this@OkHttpWebsocketEventClient.send(unsubscribeMessage)
//                    // Remove event reference from self
//                    activeFlows.remove(listenerId)
//                }
//            }
//        }
//    }
}

/**
 * Utility for managing event client flows
 */
internal fun EventClient.createFlow(
    activeFlows: MutableMap<String, (ByteArray) -> Unit>,
    subscribeMessage: ByteArray,
    unsubscribeMessage: ByteArray,
    listenerId: String
): Flow<ByteArray> {
    return callbackFlow {
        // Register event for self
        activeFlows[listenerId] = {
            trySendBlocking(it)
        }
        // Tell the server to start sending events
        this@createFlow.send(subscribeMessage)
        awaitClose {
            launch {
                // Tell the server to stop sending events
                this@createFlow.send(unsubscribeMessage)
                // Remove event reference from self
                activeFlows.remove(listenerId)
            }
        }
    }
}