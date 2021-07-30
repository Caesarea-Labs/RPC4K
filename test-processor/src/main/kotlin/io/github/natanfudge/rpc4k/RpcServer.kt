package io.github.natanfudge.rpc4k

import io.github.natanfudge.rpc4k.impl.GeneratedServerImplSuffix
import io.github.natanfudge.rpc4k.impl.splitJoinedAndDecode
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json


interface SerializationFormat {
    fun <T> encode(serializer: SerializationStrategy<T>, value: T): ByteArray
    fun <T> decode(serializer: DeserializationStrategy<T>, raw: ByteArray): T
}


//
object JsonFormat : SerializationFormat {
    private val json = Json
    private val charset = Charsets.UTF_8
    override fun <T> encode(serializer: SerializationStrategy<T>, value: T): ByteArray {
        return json.encodeToString(serializer, value).toByteArray()
    }

    override fun <T> decode(serializer: DeserializationStrategy<T>, raw: ByteArray): T {
        return json.decodeFromString(serializer, raw.toString(charset))
    }
}


interface ProtocolDecoder<P> {
    fun accept(route: String, args: List<ByteArray>): ByteArray
}


class RpcServer<T> private constructor(
    private val decoder: ProtocolDecoder<T>,
    //TODO: configurable
    private val format: SerializationFormat = JsonFormat
) {

    companion object {
        fun <T> start(decoder: ProtocolDecoder<T>): RpcServer<T> {
            return RpcServer(decoder).apply { start() }
        }

        inline fun <reified T : Any> jvmStartWithProtocol(
            protocolImpl: T,
            format: SerializationFormat = JsonFormat
        ): RpcServer<T> {
            val protocolClass = T::class
            val decoder = Class.forName(protocolClass.qualifiedName + GeneratedServerImplSuffix)
                .getDeclaredConstructor(protocolClass.java, SerializationFormat::class.java)
                .newInstance(protocolImpl, format) as ProtocolDecoder<T>
            return start(decoder)
        }
    }

    private fun start() {
        testRpcServer = this
    }

    internal fun accept(route: String, body: ByteArray): ByteArray {
        return decoder.accept(route, body.splitJoinedAndDecode())
    }
}