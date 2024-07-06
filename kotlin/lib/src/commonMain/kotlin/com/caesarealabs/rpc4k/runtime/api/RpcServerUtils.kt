package com.caesarealabs.rpc4k.runtime.api

import com.caesarealabs.rpc4k.runtime.implementation.Rpc4kLogger
import com.caesarealabs.rpc4k.runtime.implementation.RpcResult

public object RpcServerUtils {
    public suspend fun routeCall(input: ByteArray, config: ServerConfig): RpcResult {
        try {
            val method = Rpc.peekMethodName(input)
            val response = (config.router as RpcRouter<Any?>).routeRequest(input, method, config)?.let { RpcResult.Success(it) }
                ?: return RpcResult.Error("Non existent procedure $method", RpcError.InvalidRequest)
            return response
        } catch (e: InvalidRpcRequestException) {
            Rpc4kLogger.warn("Invalid request", e)
            // RpcServerException messages are trustworthy
            return RpcResult.Error(e.message, RpcError.InvalidRequest)
        } catch (e: Throwable) {
            Rpc4kLogger.error("Failed to handle request", e)
            // Don't send arbitrary throwable messages because it could leak data
            return RpcResult.Error("Server failed to process request", RpcError.InternalError)
        }
    }
}