package com.caesarealabs.rpc4k.runtime.implementation

import com.caesarealabs.rpc4k.runtime.api.*

// This handles reading and responding for both types of engines
internal suspend fun <RpcDef, I, O, Engine : RpcServerEngine.SingleCall<I, O>> RpcServerSetup<RpcDef, Engine>.handleImpl(input: I, output: O?): O? {
    try {
        val bytes = engine.read(input)

        val method = Rpc.peekMethodName(bytes)
        val response = generatedHelper.handle(bytes, method, this)
            ?: return engine.genericErrorResponder("Non existent procedure $method", RpcError.InvalidRequest, output)
        return engine.genericResponder(response, output)
    } catch (e: InvalidRpcRequestException) {
        Rpc4K.Logger.warn("Invalid request", e)
        // RpcServerException messages are trustworthy
        return engine.genericErrorResponder(e.message, RpcError.InvalidRequest, output)
    } catch (e: Throwable) {
        Rpc4K.Logger.error("Failed to handle request", e)
        // Don't send arbitrary throwable messages because it could leak data
        return engine.genericErrorResponder("Server failed to process request", RpcError.InternalError, output)
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
