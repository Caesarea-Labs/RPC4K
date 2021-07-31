package io.github.natanfudge.rpc4k

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json

object InMemoryHttpServer : HttpServer {
    private lateinit var handler: (route: String, body: ByteArray) -> ByteArray
    private lateinit var flowHandler: (route: String, body: ByteArray) -> Flow<ByteArray>
    override fun handleRequests(handler: (route: String, body: ByteArray) -> ByteArray) {
        this.handler = handler
    }

    override fun handleFlowRequests(handler: (route: String, body: ByteArray) -> Flow<ByteArray>) {
        this.flowHandler = handler
    }

    internal fun testClientHookForRequest(route: String, body: ByteArray): ByteArray {
        return handler(route, body)
    }

    internal fun testClientHookForFlowRequest(route: String, body: ByteArray): Flow<ByteArray> {
        return flowHandler(route, body)
    }
}

object InMemoryHttpClient : HttpClient {
    override fun request(route: String, body: ByteArray): ByteArray {
        return InMemoryHttpServer.testClientHookForRequest(route, body)
    }

    override fun flowRequest(route: String, body: ByteArray): Flow<ByteArray> {
        return InMemoryHttpServer.testClientHookForFlowRequest(route, body)
    }
}

object JsonFormat : SerializationFormat {
    private val json = Json
    private val charset = Charsets.UTF_8
    override fun <T> encode(serializer: SerializationStrategy<T>, value: T): ByteArray {
        return json.encodeToString(serializer, value).toByteArray(charset)
    }

    override fun <T> decode(serializer: DeserializationStrategy<T>, raw: ByteArray): T {
        return json.decodeFromString(serializer, raw.toString(charset))
    }
}