@file:Suppress("unused")

package io.github.natanfudge.rpc4k.impl

import io.github.natanfudge.rpc4k.MalformedRequestException
import io.github.natanfudge.rpc4k.RpcClient
import io.github.natanfudge.rpc4k.SerializationFormat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy

object Rpc4kGeneratedServerUtils {
    fun <T> encodeResponse(format: SerializationFormat, serializer: SerializationStrategy<T>, response: T) =
        format.encode(serializer, response)
    fun <T> encodeFlowResponse(format: SerializationFormat, serializer: SerializationStrategy<T>, response: Flow<T>) =
        response.map {  format.encode(serializer, it) }

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
        val body = encodeBody(parameters, client)
        val response = client.http.request(route, body)
        return client.format.decode(returnType, response)
    }

    fun <T> sendFlow(
        client: RpcClient,
        route: String,
        parameters: List<Pair<Any?, KSerializer<out Any?>>>,
        returnType: KSerializer<T>
    ): Flow<T> {
        val body = encodeBody(parameters, client)
        val response = client.http.flowRequest(route, body)
        return response.map { client.format.decode(returnType, it) }
    }

    private fun encodeBody(
        parameters: List<Pair<Any?, KSerializer<out Any?>>>,
        client: RpcClient
    ): ByteArray {
        val body = parameters
            .map { (value, serializer) ->
                @Suppress("UNCHECKED_CAST")
                client.format.encode(serializer as KSerializer<Any?>, value)
            }
            .encodeAndJoin()
        return body
    }
//
//    fun <T, R> sendAny(
//        client: RpcClient,
//        route: String,
//        parameters: List<Pair<Any?, KSerializer<out Any?>>>,
//        returnType: KSerializer<T>,
//        sender: (String, ByteArray) -> R
//    ): T {
//        val body = parameters
//            .map { (value, serializer) ->
//                @Suppress("UNCHECKED_CAST")
//                client.format.encode(serializer as KSerializer<Any?>, value)
//            }
//            .encodeAndJoin()
//        val response = sender(route, body)
//        return client.format.decode(returnType, response)
//    }

}
