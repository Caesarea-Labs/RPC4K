package io.github.natanfudge.rpc4k.impl

import io.github.natanfudge.rpc4k.MalformedRequestException
import io.github.natanfudge.rpc4k.RpcClient
import io.github.natanfudge.rpc4k.SerializationFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

object Rpc4kGeneratedServerUtils {
    fun <T> encodeResponse(format: SerializationFormat, serializer: SerializationStrategy<T>, response: T) =
        format.encode(serializer, response)

    fun <T> decodeParameter(format: SerializationFormat, serializer: DeserializationStrategy<T>, raw: ByteArray) =
        format.decode(serializer, raw)

    fun invalidRoute(route: String): Nothing = throw MalformedRequestException("Unexpected route: $route")
}


object Rpc4KGeneratedClientUtils {
    fun <T> send(
        client: RpcClient,
        route: String,
        parameters: List<Pair<Any?, KSerializer<out Any?>>>,
        returnType: KSerializer<T>
    ): T {
//        val body = parameters.joinToString(argumentSeparator.toString()) { (value, serializer) ->
//            @Suppress("UNCHECKED_CAST")
//            client.format.encode(serializer as KSerializer<Any?>, value)
//        }

        val body = parameters
            .map { (value, serializer) -> client.format.encode(serializer as KSerializer<Any?>, value) }
            .encodeAndJoin()
        val response = client.send(route, body)
        return client.format.decode(returnType, response)
    }
}

internal val json = Json
internal const val argumentSeparator = ','
