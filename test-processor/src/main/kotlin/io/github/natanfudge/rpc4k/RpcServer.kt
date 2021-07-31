package io.github.natanfudge.rpc4k

import io.github.natanfudge.rpc4k.impl.GeneratedServerImplSuffix
import io.github.natanfudge.rpc4k.impl.splitJoinedAndDecode
import kotlinx.coroutines.flow.Flow


interface ProtocolDecoder<P> {
    fun accept(route: String, args: List<ByteArray>): Any
}

interface HttpServer {
    fun handleRequests(handler: (route: String, body: ByteArray) -> ByteArray)
    fun handleFlowRequests(handler: (route: String, body: ByteArray) -> Flow<ByteArray>)
}

class RpcServer<T> private constructor(
    private val decoder: ProtocolDecoder<T>,
    httpServer: HttpServer
) {
    init {
        httpServer.handleRequests { route, body -> decoder.accept(route, body.splitJoinedAndDecode()) as ByteArray }
        httpServer.handleFlowRequests { route, body ->
            decoder.accept(
                route,
                body.splitJoinedAndDecode()
            ) as Flow<ByteArray>
        }
    }

    companion object {
        fun <T> start(decoder: ProtocolDecoder<T>, http: HttpServer): RpcServer<T> {
            return RpcServer(decoder, http).apply { start() }
        }

        inline fun <reified T : Any> jvmStartWithProtocol(
            protocolImpl: T,
            format: SerializationFormat = JsonFormat,
            http: HttpServer
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