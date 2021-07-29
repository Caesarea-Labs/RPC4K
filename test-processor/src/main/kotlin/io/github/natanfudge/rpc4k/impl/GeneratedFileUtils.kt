package io.github.natanfudge.rpc4k.impl

import io.github.natanfudge.rpc4k.MalformedRequestException
import io.github.natanfudge.rpc4k.RpcClient
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

object Rpc4kGeneratedServerUtils {
    fun <T> encodeResponse(serializer: SerializationStrategy<T>, response: T) =
        json.encodeToString(serializer, response)

    fun <T> decodeParameter(serializer: DeserializationStrategy<T>, raw: JsonElement) =
        json.decodeFromJsonElement(serializer, raw)

    fun invalidRoute(route: String): Nothing = throw MalformedRequestException("Unexpected route: $route")
}


object Rpc4KGeneratedClientUtils {
    fun <T> send(
        client: RpcClient,
        route: String,
        parameters: List<Pair<Any, KSerializer<out Any>>>,
        returnType: KSerializer<T>
    ): T {
        val body = parameters.joinToString(argumentSeparator.toString()) { (value, serializer) ->
            @Suppress("UNCHECKED_CAST")
            json.encodeToString(serializer as KSerializer<Any>, value)
        }
        val response = client.send(route, body)
        return json.decodeFromString(returnType, response)
    }
}

internal val json = Json
internal const val argumentSeparator = ','
