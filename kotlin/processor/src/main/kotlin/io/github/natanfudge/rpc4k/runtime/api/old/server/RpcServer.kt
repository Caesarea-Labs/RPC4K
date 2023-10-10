package io.github.natanfudge.rpc4k.runtime.api.old.server

import io.github.natanfudge.rpc4k.runtime.api.old.format.JsonFormat
import io.github.natanfudge.rpc4k.runtime.api.old.utils.DecoderContext
import io.github.natanfudge.rpc4k.runtime.api.old.utils.Interceptors
import io.github.natanfudge.rpc4k.processor.old.GeneratedServerImplSuffix
import io.github.natanfudge.rpc4k.runtime.api.old.format.SerializationFormat
import io.github.natanfudge.rpc4k.runtime.implementation.old.splitJoinedAndDecode
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass


interface ProtocolDecoder<P> {
    suspend fun accept(route: String, args: List<ByteArray>): Any
}

typealias RequestHandler = suspend (route: String, body: ByteArray) -> ByteArray
typealias FlowRequestHandler = suspend (route: String, body: ByteArray) -> Flow<ByteArray>

interface RpcHttpServer {
    fun start(requestHandler: RequestHandler, flowRequestHandler: FlowRequestHandler)
    fun stop()
}

class RpcServer<T>(
    private val decoder: ProtocolDecoder<T>,
    private val httpServer: RpcHttpServer
) {

    private suspend fun decode(route: String, body: ByteArray): Any {
        val parameters = try {
            // See JoinableEncoding
            body.splitJoinedAndDecode()
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid request encoding", e)
        }
        return decoder.accept(
            route,
            // Parameters are joined using a special encoding
            parameters
        )
    }

    @Suppress("unchecked_cast")
    fun start() {
        httpServer.start(
            requestHandler = { route, body ->
                decode(route, body) as ByteArray
            },

            flowRequestHandler = { route, body ->
                decode(route, body) as Flow<ByteArray>
            }
        )
    }

    fun stop() {
        httpServer.stop()
    }

    companion object {
        inline fun <reified T : Any> jvmWithProtocol(
            protocolImpl: T,
            http: RpcHttpServer,
            format: SerializationFormat = JsonFormat,
            interceptors: Interceptors = Interceptors(listOf())
        ): RpcServer<T> = jvmWithProtocol(protocolImpl, http, T::class, interceptors, format)

        @Suppress("unchecked_cast")
        fun <T : Any> jvmWithProtocol(
            protocolImpl: T,
            http: RpcHttpServer,
            protocolClass: KClass<T>,
            interceptors: Interceptors = Interceptors(listOf()),
            format: SerializationFormat = JsonFormat,
        ): RpcServer<T> {
            val decoder = Class.forName(protocolClass.qualifiedName + GeneratedServerImplSuffix)
                .getDeclaredConstructor(protocolClass.java, DecoderContext::class.java)
                .newInstance(protocolImpl, DecoderContext(format, interceptors)) as ProtocolDecoder<T>
            return RpcServer(decoder, http)
        }
    }
}