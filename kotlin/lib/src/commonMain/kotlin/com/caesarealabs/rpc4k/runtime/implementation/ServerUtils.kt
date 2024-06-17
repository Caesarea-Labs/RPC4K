@file:Suppress("UNCHECKED_CAST")

package com.caesarealabs.rpc4k.runtime.implementation

import com.caesarealabs.rpc4k.runtime.api.*
import com.caesarealabs.rpc4k.runtime.user.Rpc4kIndex


// This handles reading and responding for both types of engines
internal suspend fun <I, O> RpcServerEngine.SingleCall<I, O>.routeRpcCallImpl(input: I, output: O?, config: ServerConfig): O? {
    try {
        val bytes = read(input)

        val method = Rpc.peekMethodName(bytes)
        val response = (config.router as RpcRouter<Any?>).routeRequest(bytes, method, config)
            ?: return genericErrorResponder("Non existent procedure $method", RpcError.InvalidRequest, output)
        return genericResponder(response, output)
    } catch (e: InvalidRpcRequestException) {
        Rpc4kLogger.warn("Invalid request", e)
        // RpcServerException messages are trustworthy
        return genericErrorResponder(e.message, RpcError.InvalidRequest, output)
    } catch (e: Throwable) {
        Rpc4kLogger.error("Failed to handle request", e)
        // Don't send arbitrary throwable messages because it could leak data
        return genericErrorResponder("Server failed to process request", RpcError.InternalError, output)
    }
}

// Generically handle the methods of both the Writing and Returning server types
private suspend fun <I, O> RpcServerEngine.SingleCall<I, O>.genericResponder(input: ByteArray, output: O?): O? = when (this) {
    is RpcServerEngine.SingleCall.Returning -> respond(input)
    is RpcServerEngine.SingleCall.Writing -> write(input, output!!).let { null }
}

private suspend fun <I, O> RpcServerEngine.SingleCall<I, O>.genericErrorResponder(message: String, errorType: RpcError,
                                                                                  output: O?): O? = when (this) {
    is RpcServerEngine.SingleCall.Returning -> respondError(message, errorType)
    is RpcServerEngine.SingleCall.Writing -> writeError(message, errorType, output!!).let { null }
}

internal fun <S, I> Rpc4kIndex<S, *, I>.createHandlerConfig(
    format: SerializationFormat,
    eventManager: EventManager,
    engine: RpcServerEngine,
    service: (I) -> S
): HandlerConfigImpl<S, I> {
    @Suppress("RemoveExplicitTypeArguments")
    // The type args are necessary.
    return HandlerConfigImpl<S, I>({ service(it) }, { createInvoker(it) }, format, eventManager, engine)
}


