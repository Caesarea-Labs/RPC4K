package io.github.natanfudge.rpc4k

import io.github.natanfudge.rpc4k.impl.GeneratedServerImplSuffix
import io.github.natanfudge.rpc4k.impl.splitJoinedAndDecode
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json


interface SerializationFormat {
    fun <T> encode(serializer: SerializationStrategy<T>, value: T): ByteArray
    fun <T> decode(serializer: DeserializationStrategy<T>, raw: ByteArray): T
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


interface ProtocolDecoder<P> {
    fun  accept(route: String, args: List<ByteArray>): Any
}

interface HttpServer {
    fun handleRequests(handler: (route: String, body: ByteArray) -> ByteArray)
    fun handleFlowRequests(handler: (route: String, body: ByteArray) -> Flow<ByteArray>)
}

object TestHttpServer : HttpServer {
    private lateinit var handler: (route: String, body: ByteArray) -> ByteArray
    private lateinit var flowHandler: (route: String, body: ByteArray) -> Flow<ByteArray>
    override fun handleRequests(handler: (route: String, body: ByteArray) -> ByteArray) {
        this.handler = handler
    }

    override fun handleFlowRequests(handler: (route: String, body: ByteArray) -> Flow<ByteArray>) {
        this.flowHandler = handler
    }

    fun testClientHookForRequest(route: String, body: ByteArray) : ByteArray {
        return handler(route, body)
    }

    fun testClientHookForFlowRequest(route: String, body: ByteArray) : Flow<ByteArray> {
        return flowHandler(route, body)
    }
}

class RpcServer<T> private constructor(
    private val decoder: ProtocolDecoder<T>,
    httpServer: HttpServer
) {
    init {
        httpServer.handleRequests { route, body ->  decoder.accept(route, body.splitJoinedAndDecode()) as ByteArray}
        httpServer.handleFlowRequests { route, body ->  decoder.accept(route, body.splitJoinedAndDecode()) as Flow<ByteArray>}
    }

    companion object {
        fun <T> start(decoder: ProtocolDecoder<T>, http: HttpServer): RpcServer<T> {
            return RpcServer(decoder,http).apply { start() }
        }

        inline fun <reified T : Any> jvmStartWithProtocol(
            protocolImpl: T,
            format: SerializationFormat = JsonFormat,
            http: HttpServer = TestHttpServer
        ): RpcServer<T> {
            val protocolClass = T::class
            val decoder = Class.forName(protocolClass.qualifiedName + GeneratedServerImplSuffix)
                .getDeclaredConstructor(protocolClass.java, SerializationFormat::class.java)
                .newInstance(protocolImpl, format) as ProtocolDecoder<T>
            return start(decoder, http)
        }
    }

    private fun start() {
        testRpcServer = this
    }

}