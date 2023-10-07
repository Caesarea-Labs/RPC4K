package io.github.natanfudge.rpc4k.runtime.api

import kotlinx.serialization.SerializationStrategy

/**
 * Generic interface for anything that sends information across the network.
 * Examples: HTTP Client, HTTP Server, WebSocket client/server.
 */
interface RpcClient {
    /**
     * Sends something across the network. If there's a response, this returns a ByteArray.
     * The transmitter should use the specified [format] with the specified [serializers] to serialize the arguments of [rpc].
     * In the future we could allow [RpcClient]s to have custom logic when it comes to how exactly to serialize the [rpc].
     *
     * @return If there's a response (like in REST) returns the response body's bytes. If there's no response (like in WebSocket) returns null.
     */
    suspend fun send(rpc: Rpc, format: SerializationFormat, serializers: List<SerializationStrategy<*>>): ByteArray?
}

interface RespondingRpcClient : RpcClient {
    /**
     * Sends something across the network. Since there's always a response, this returns a ByteArray.
     * @see RpcClient
     * @return The response body's bytes.
     */
    override suspend fun send(rpc: Rpc, format: SerializationFormat, serializers: List<SerializationStrategy<*>>): ByteArray
}


