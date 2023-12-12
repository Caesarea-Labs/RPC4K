package com.caesarealabs.rpc4k.runtime.api.components

import com.caesarealabs.rpc4k.runtime.api.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.KSerializer
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import kotlin.coroutines.resume

class OkHttpRpcClient(private val url: String, private val client: OkHttpClient = OkHttpClient()) : RpcClient {

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