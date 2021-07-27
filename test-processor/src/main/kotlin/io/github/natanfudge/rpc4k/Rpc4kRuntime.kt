package io.github.natanfudge.rpc4k

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass


interface ProtocolDecoder<T> {
    fun accept(route: String, args: List<String>): String
}

class RpcServer<T> private constructor(private val decoder: ProtocolDecoder<T>) {
    companion object {
        fun <T> start(decoder: ProtocolDecoder<T>): RpcServer<T> {
            return RpcServer(decoder).apply { start() }
        }
    }

    private fun start() {
        testRpcServer = this
    }

    internal fun accept(route: String, body: String): String {
        return decoder.accept(route, body.split(argumentSeparator))
    }
}

lateinit var testRpcServer: RpcServer<*>

class RpcClient {
    companion object {
        fun <T : Any> jvmWithProtocol(protocolClass: KClass<T>): T {
            return Class.forName(protocolClass.qualifiedName + GeneratedClientImplSuffix)
                .getDeclaredConstructor(RpcClient::class.java)
                .newInstance(RpcClient()) as T
        }
    }

    fun send(route: String, body: String): String {
        return testRpcServer.accept(route, body)
    }
}

object Rpc4kGeneratedServerUtils {
    fun <T> encodeResponse(serializer: SerializationStrategy<T>, response: T) =
        json.encodeToString(serializer, response)

    fun <T> decodeParameter(serializer: DeserializationStrategy<T>, raw: String) =
        json.decodeFromString(serializer, raw)

    fun invalidRoute(route: String): Nothing = throw MalformedRequestException("Unexpected route: $route")
}


object Rpc4KGeneratedClientUtils {
    fun <T> send(
        client: RpcClient,
        route: String,
        parameters: List<Pair<Any, KSerializer<out Any>>>,
        returnType: KSerializer<T>
    ): T {
        val response = client.send(route, parameters.joinToString(argumentSeparator) { (value, serializer) ->
            @Suppress("UNCHECKED_CAST")
            json.encodeToString(serializer as KSerializer<Any>, value)
        })
        return json.decodeFromString(returnType, response)
    }
}

private const val argumentSeparator = ","

class MalformedRequestException(message: String) : Exception(message)

private val json = Json
