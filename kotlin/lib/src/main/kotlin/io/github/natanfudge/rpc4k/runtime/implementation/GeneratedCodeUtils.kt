package io.github.natanfudge.rpc4k.runtime.implementation

import io.github.natanfudge.rpc4k.runtime.api.*
import kotlinx.serialization.*

sealed interface GeneratedHandlerResult<T> {
    data class Success<T>(val value: T): GeneratedHandlerResult<T>
    data class Error(val message: String, val type: RpcError)
}

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


//    /**
//     * Catches rpc exceptions and sends the correct error back to the client
//     */
//    suspend inline fun withCatching(server: RpcServer, handler: () -> Unit) {
//        try {
//            handler()
//        } catch (e: RpcServerException) {
//            Rpc4K.Logger.warn("Invalid request", e)
//            // RpcServerException messages are trustworthy
//            server.sendError(e.message, RpcError.InvalidRequest)
//        } catch (e: Throwable) {
//            Rpc4K.Logger.error("Failed to handle request", e)
//            // Don't send arbitrary throwable messages because it could leak data
//            server.sendError("Server failed to process request", RpcError.InternalError)
//        }
//    }

    /**
     * Sends a value, not caring about the result
     */
    suspend fun send(client: RpcClient, format: SerializationFormat, methodName: String, args: List<Any?>, argSerializers: List<KSerializer<*>>) {
        val rpc = Rpc(methodName, args)
        client.send(rpc, format, argSerializers)
    }



//    //    internal abstract fun errorResponse(message: String, errorType: RpcError): O
//    fun <I,O>respond(input: I): O {
//        val bytes = read(input)
//
//        try {
//            return handleImplementation(input)
//        } catch (e: InvalidRpcRequestException) {
//            Rpc4K.Logger.warn("Invalid request", e)
//            // RpcServerException messages are trustworthy
//            return errorResponse(e.message, RpcError.InvalidRequest)
//        } catch (e: Throwable) {
//            Rpc4K.Logger.error("Failed to handle request", e)
//            // Don't send arbitrary throwable messages because it could leak data
//            return errorResponse("Server failed to process request", RpcError.InternalError)
//        }
//    }

//    /**
//     * Uses the [server] to respond with the specified data
//     */
//    suspend fun <T> respond(
//        format: SerializationFormat,
//        server: RpcServer,
//        request: ByteArray,
//        argDeserializers: List<KSerializer<*>>,
//        resultSerializer: KSerializer<T>,
//        respondMethod: suspend (args: List<*>) -> T
//    ) {
//        val parsed = try {
//            Rpc.fromByteArray(request, format, argDeserializers)
//        } catch (e: SerializationException) {
//            throw InvalidRpcRequestException("Malformed request arguments: ${e.message}", e)
//        }
//        val result = respondMethod(parsed.arguments)
//        server.send(format, result, resultSerializer)
//    }
    /**
     * Uses the [server] to respond with the specified data
     */
    suspend fun <T> respond(
        setup: RpcServerSetup<*,*>,
//        config: RpcSettings,
//        server: RpcServer,
        request: ByteArray,
        argDeserializers: List<KSerializer<*>>,
        resultSerializer: KSerializer<T>,
        respondMethod: suspend (args: List<*>) -> T
    ): ByteArray {
        val parsed = try {
            Rpc.fromByteArray(request, setup.format, argDeserializers)
        } catch (e: SerializationException) {
            throw InvalidRpcRequestException("Malformed request arguments: ${e.message}", e)
        }

        return setup.format.encode(resultSerializer, respondMethod(parsed.arguments))
    }

}

