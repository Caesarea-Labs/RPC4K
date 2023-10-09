package io.github.natanfudge.rpc4k.runtime.implementation

import io.github.natanfudge.rpc4k.runtime.api.*
import kotlinx.serialization.KSerializer

/**
 * These functions are used by generated code
 */
object GeneratedCodeUtils {
    /**
     * Sends a value and returns the result
     */
    suspend fun <T> request(
        client: RpcClient,
        format: SerializationFormat,
        methodName: String,
        args: List<*>,
        argSerializers: List<KSerializer<*>>,
        responseSerializer: KSerializer<T>
    ): T {
        val rpc = Rpc(methodName, args)
        val result = client.send(rpc, format, argSerializers)
        return format.decode(responseSerializer, result)
    }

    /**
     * Sends a value, not caring about the result
     */
    suspend fun send(client: RpcClient, format: SerializationFormat, methodName: String, args: List<Any?>, argSerializers: List<KSerializer<*>>) {
        val rpc = Rpc(methodName, args)
        client.send(rpc, format, argSerializers)
    }

    /**
     * Catches rpc exceptions and sends the correct error back to the client
     */
    suspend inline fun withCatching(server: RpcServer, handler: () -> Unit) {
        try {
            handler()
        } catch (e: RpcServerException) {
            server.sendError(e.message, RpcError.InvalidRequest)
        } catch (e: Throwable) {
            server.sendError(e.message ?: "", RpcError.InternalError)
        }
    }

    /**
     * Uses the [server] to respond with the specified data
     */
    suspend fun <T> respond(
        format: SerializationFormat,
        server: RpcServer,
        request: ByteArray,
        argDeserializers: List<KSerializer<*>>,
        resultSerializer: KSerializer<T>,
        respondMethod: suspend (args: List<*>) -> T
    ) {
        val parsed = Rpc.fromByteArray(request, format, argDeserializers)
        val result = respondMethod(parsed.arguments)
        server.send(format, result, resultSerializer)
    }
}

interface GeneratedServerHandler {
    suspend fun handle(request: ByteArray, method: String)
}

interface GeneratedServerHandlerFactory<Api> {
    fun build(api: Api, format: SerializationFormat, server: RpcServer): GeneratedServerHandler
}