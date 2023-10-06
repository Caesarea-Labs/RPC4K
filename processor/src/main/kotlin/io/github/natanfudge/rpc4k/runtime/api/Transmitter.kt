package io.github.natanfudge.rpc4k.runtime.api

import io.github.natanfudge.rpc4k.runtime.api.components.OkHttpClientTransmitter
import io.github.natanfudge.rpc4k.runtime.api.format.JsonFormat
import io.github.natanfudge.rpc4k.runtime.api.format.SerializationFormat
import io.github.natanfudge.rpc4k.runtime.impl.Argument
import io.github.natanfudge.rpc4k.runtime.impl.Rpc
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

/**
 * Generic interface for anything that sends information across the network.
 * Examples: HTTP Client, HTTP Server, WebSocket client/server.
 */
interface Transmitter {
    /**
     * Sends something across the network. If there's a response, this returns a ByteArray.
     */
    suspend fun send(rpc: Rpc, format: SerializationFormat): ByteArray?
}

class RpcConfiguration(val transmitter: Transmitter, val format: SerializationFormat)

fun main(): Unit = runBlocking {
    MyApi(RpcConfiguration(OkHttpClientTransmitter(""), JsonFormat)).getDogs(2, "Asdf")
}
//TODO:


class MyApi(config: RpcConfiguration) : GeneratedApiHandler(config) {
    suspend fun getDogs(num: Int, type: String): Dog {
        val arg1Serialized = Argument(num, Int.serializer())
        val arg2Serialized = Argument(type, String.serializer())
        val combined = Rpc("getDogs", listOf(arg1Serialized, arg2Serialized))
        val bytesString = combined.toByteArray(JsonFormat).decodeToString()
        TODO()

    }
}

fun <T> GeneratedApiHandler.encode(serializer: KSerializer<T>, argument: T): ByteArray = config.format.encode(serializer, argument)

abstract class GeneratedApiHandler(val config: RpcConfiguration)

@Serializable
data class Dog(val name: String, val type: String, val age: Int)