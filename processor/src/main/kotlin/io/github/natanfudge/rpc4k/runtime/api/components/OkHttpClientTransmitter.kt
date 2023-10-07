package io.github.natanfudge.rpc4k.runtime.api.components

import io.github.natanfudge.rpc4k.runtime.api.Transmitter
import io.github.natanfudge.rpc4k.runtime.api.format.SerializationFormat
import io.github.natanfudge.rpc4k.runtime.impl.Rpc
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.SerializationStrategy
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import kotlin.coroutines.resume

class OkHttpClientTransmitter(private val url: String, private val client: OkHttpClient = OkHttpClient()) : Transmitter {

    override suspend fun send(rpc: Rpc, format: SerializationFormat, serializers: List<SerializationStrategy<*>>): ByteArray {
        val data = rpc.toByteArray(format, serializers)
        val response = client.request(Request(url.toHttpUrl(), body = data.toRequestBody()))
        when (response.code) {
            200 -> return response.body.bytes()
            400 -> throw IllegalArgumentException("Server returned 400 - request was not valid. " +
                    "The client may not be up to date. Request: $data, Response: ${response.body.string()}"
            )

            404 -> error("404 - Could not find the server at url '$url'.")
            500 -> error("The server crashed handling the request. Request: $data, Response: ${response.body.string()}")
            else -> error("The server returned an unexpected status code: ${response.code}. Request: $data, Response: ${response.body.string()}")
        }
    }
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