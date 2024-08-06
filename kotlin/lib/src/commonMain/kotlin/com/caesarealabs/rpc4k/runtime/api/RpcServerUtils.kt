package com.caesarealabs.rpc4k.runtime.api

import com.caesarealabs.logging.Logging
import com.caesarealabs.logging.PrintLogging
import com.caesarealabs.rpc4k.runtime.implementation.RpcResult
import com.caesarealabs.rpc4k.runtime.user.RPCContext

public object RpcServerUtils {
    /**
     * Should be called by server implementations whenever a new request is received, and pass the request's bytes as [input].
     * A response must then be returned to the client according to the [RpcResult].
     * This will call the RPC Service, using code gen and such.
     *
     * @param serverData Information specific to the server implementation calling this. RPC services may then
     * reference this data through [RPCContext.serverData] (usually needing an `is` check specific to the server implementation)
     */
    public suspend fun routeCall(input: ByteArray, config: ServerConfig, serverData: Any? = null): RpcResult {
        // Logging not available yet - make do with normal prints
        val method = try {
            Rpc.peekMethodName(input)
        } catch (e: InvalidRpcRequestException) {
            return invalidRequest(e, PrintLogging)
        } catch (e: Throwable) {
            return serverError(e, PrintLogging)
        }
        // Logging available
        return config.config.logging.wrapCall(method) {
            val logging = this@wrapCall
            try {
                (config.router as RpcRouter<Any?>).routeRequest(input, method, config.config, SimpleRpcContext(serverData, logging))
                    ?.let { RpcResult.Success(it) }
                    ?: RpcResult.Error("Non existent procedure $method", RpcError.InvalidRequest)
            } catch (e: InvalidRpcRequestException) {
                invalidRequest(e, logging)
            } catch (e: Throwable) {
                serverError(e, logging)
            }
        }
    }

    private fun invalidRequest(exception: InvalidRpcRequestException, logging: Logging): RpcResult {
        logging.logWarn(exception) { "Invalid request" }
        // RpcServerException messages are trustworthy
        return RpcResult.Error(exception.message, RpcError.InvalidRequest)
    }

    private fun serverError(exception: Throwable, logging: Logging): RpcResult {
        logging.logError(exception) { "Failed to handle request" }
        // Don't send arbitrary throwable messages because it could leak data
        return RpcResult.Error("Server failed to process request", RpcError.InternalError)
    }
}

public class SimpleRpcContext(override val serverData: Any?, private val logging: Logging) : RPCContext, Logging by logging





