package io.github.natanfudge.rpc4k.runtime.implementation

import io.github.natanfudge.rpc4k.runtime.api.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import org.slf4j.LoggerFactory

/**
 * These functions are used by generated code and code that interacts with them
 */
object GeneratedCodeUtils {
    const val FactoryName = "Factory"
    const val ClientSuffix = "ClientImpl"
    const val ServerSuffix = "ServerImpl"
    const val Package = "io.github.natanfudge.rpc4k.generated"


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

    @PublishedApi
    internal val logger = LoggerFactory.getLogger("RPC4K")

    /**
     * Catches rpc exceptions and sends the correct error back to the client
     */
    suspend inline fun withCatching(server: RpcServer, handler: () -> Unit) {
        try {
            handler()
        } catch (e: RpcServerException) {
            logger.warn("Invalid request", e)
            // RpcServerException messages are trustworthy
            server.sendError(e.message, RpcError.InvalidRequest)
        } catch (e: Throwable) {
            logger.error("Failed to handle request", e)
            // Don't send arbitrary throwable messages because it could leak data
            server.sendError("Server failed to process request", RpcError.InternalError)
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
        val parsed = try {
            Rpc.fromByteArray(request, format, argDeserializers)
        } catch (e: SerializationException) {
            throw RpcServerException("Malformed request arguments: ${e.message}", e)
        }
        val result = respondMethod(parsed.arguments)
        server.send(format, result, resultSerializer)
    }
}

