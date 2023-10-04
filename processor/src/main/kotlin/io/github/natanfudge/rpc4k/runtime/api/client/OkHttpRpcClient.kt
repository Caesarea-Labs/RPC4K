package io.github.natanfudge.rpc4k.runtime.api.client

import io.github.natanfudge.rpc4k.runtime.api.Logger
import io.github.natanfudge.rpc4k.runtime.api.Port
import io.github.natanfudge.rpc4k.runtime.impl.Sse
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.takeWhile
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class OkHttpRpcClient(private val logger: Logger, port: Port) :
    RpcHttpClient {
    private val client = OkHttpClient.Builder().readTimeout(30, TimeUnit.MINUTES)
        .build()

    private val url = "http://localhost:${port.value}"

    override suspend fun request(route: String, body: ByteArray): ByteArray {
        return client.postBytes("$url/$route", body.toString(Charsets.UTF_8))
    }

    override suspend fun flowRequest(route: String, body: ByteArray): Flow<ByteArray> {
        return client.sseImpl("$url/flow/$route", body.toString(Charsets.UTF_8), logger)
            .takeWhile { it != null }
            .map {
                it!!.data.toByteArray(Charsets.UTF_8)
            }
    }

}

private suspend fun OkHttpClient.post(url: String, body: String): String =
    postBytes(url, body).toString(Charsets.UTF_8)

private suspend fun OkHttpClient.postBytes(url: String, body: String): ByteArray =
    suspendCancellableCoroutine { coroutine ->
        val post = postRequest(url = url, body = body)

        newCall(post).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                coroutine.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == HttpStatusCode.OK.value) coroutine.resume(response.body!!.bytes())
                else {
                    coroutine.resumeWithException(response.statusCodeException())
                }
            }
        })
    }

private fun Response.statusCodeException() = when (code) {
    HttpStatusCode.InternalServerError.value -> InternalServerException()
    HttpStatusCode.ExpectationFailed.value -> ExpectationFailedException(message)
    HttpStatusCode.Unauthorized.value -> UnauthorizedException(message)
    else -> OtherStatusCodeException(code)
}


fun OkHttpClient.sseImpl(url: String, body: String, logger: Logger): Flow<Sse?> {
    val request = postRequest(url = url, body = body)

    return callbackFlow {
        logger.debug { "Sending a new SSE" }

        val eventSource = EventSources.createFactory(this@sseImpl)
            .newEventSource(request, sseListener(logger))

        @Suppress("EXPERIMENTAL_API_USAGE")
        awaitClose {
            logger.debug { "Cancelling SSE" }
            eventSource.cancel()
        }

    }


}

@OptIn(DelicateCoroutinesApi::class)
private fun SendChannel<Sse?>.sseListener(
    logger: Logger
) = object : EventSourceListener() {
    override fun onClosed(eventSource: EventSource) {
        println("Close")
        GlobalScope.launch {
            // Grace period to let reads be done before we close the flow
            delay(100)
            // Null response signifies that the event is closed
            trySendBlocking(null)
                .onFailure { throw it ?: Exception("Unknown Exception") }
        }

    }

    override fun onEvent(
        eventSource: EventSource,
        id: String?,
        type: String?,
        data: String
    ) {
        println("Event")
        logger.debug { "Got event AT client FROM server: $data" }
        val event = Sse(data = data, event = type, id = id)
        trySendBlocking(event)
            .onFailure { throw it ?: Exception("Unknown Exception") }
    }

    override fun onFailure(
        eventSource: EventSource,
        t: Throwable?,
        response: Response?
    ) {
        close(t ?: response?.statusCodeException())
    }

    override fun onOpen(eventSource: EventSource, response: Response) {
    }
}


private fun postRequest(url: String, body: String): Request {
    val reqBody = jsonRequestBody(body)
    return Request.Builder()
        .url(url)
        .post(reqBody)
        .build()
}

private fun jsonRequestBody(body: String) =
    body.toRequestBody("application/json".toMediaTypeOrNull())

