package com.caesarealabs.rpc4k.runtime.user.components

import com.caesarealabs.rpc4k.runtime.api.*
import com.caesarealabs.rpc4k.runtime.user.Rpc4kIndex
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.io.EOFException
import kotlinx.serialization.KSerializer


public class KtorRpcClient(
    private val url: String, private val websocketUrl: String,
    private val clientConfig: HttpClientConfig<*>.() -> Unit = {}
) : RpcClient {
    private val client = HttpClient {
        install(WebSockets)
        clientConfig()
    }

    override suspend fun send(rpc: Rpc, format: SerializationFormat, serializers: List<KSerializer<*>>): ByteArray {
        val data = rpc.toByteArray(format, serializers)
        val response = client.request(urlString = url) {
            method = HttpMethod.Post
            setBody(data)
        }

        suspend fun exception(message: String): Nothing = throw RpcResponseException(
            message, rpc, format, this, response.body(), response.status.value
        )
        when (response.status.value) {
            200 -> return response.body()
            400 -> exception("Request was not valid. The client may not be up to date")
            404 -> exception("Could not find the server at url '$url'.")
            500 -> exception("The server crashed handling the request")
            else -> exception("The server returned an unexpected status code: ${response.status}.")
        }
    }

    override val events: EventClient
        get() = KtorWebsocketEventClient(websocketUrl, client)
}


private class KtorWebsocketEventClient(
    private val url: String,
    private val client: HttpClient
) : AbstractEventClient() {
    private var websocket: WebSocketSession? = null

    // LOWPRIO: better scoping behavior, the websocket should be properly scoped by calling methods, and be turned off when the client is no longer used
    @OptIn(DelicateCoroutinesApi::class)
    private val wsScope = CoroutineScope(GlobalScope.coroutineContext)

    override suspend fun send(message: ByteArray) {
        // Not thread-safe
        if (websocket == null) {
            websocket = client.webSocketSession(urlString = url)
            wsScope.launch {
                while (true) {
                    try {
                        val othersMessage = websocket!!.incoming.receive()
                        handleMessage(S2CEventMessage.fromByteArray(othersMessage.readBytes()))
                    } catch (e: EOFException) {
                        println("Websocket connection closed")
                        break
                    }

                }
            }
        }
        websocket!!.send(message)
    }
}

public fun <C> Rpc4kIndex<*, C, *>.ktorClient(url: String, websocketUrl: String, format: SerializationFormat = JsonFormat()): C {
//    val websocketUrl = "$url/events"
    return createNetworkClient(KtorRpcClient(url, "$websocketUrl/events"), format)
}

