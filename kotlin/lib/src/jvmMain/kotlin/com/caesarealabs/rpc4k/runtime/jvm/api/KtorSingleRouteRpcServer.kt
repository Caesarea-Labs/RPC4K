package com.caesarealabs.rpc4k.runtime.jvm.api

import com.caesarealabs.rpc4k.runtime.api.*
import com.caesarealabs.rpc4k.runtime.implementation.RpcResult
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.utils.io.core.*

public sealed interface KtorRequestResult {
    public class Success(public val bytes: ByteArray): KtorRequestResult
    public class Error(public val message: String, public val statusCode: HttpStatusCode): KtorRequestResult
}


public object Rpc4kKtor {
    public suspend fun routeCalls(call: ApplicationCall, config: ServerConfig) {
        val bytes = call.receiveChannel().readRemaining().readBytes()
        when(val result = RpcServerUtils.routeCall(bytes,config)) {
            is RpcResult.Error ->  {
                val code = when (result.errorType) {
                    RpcError.InvalidRequest -> HttpStatusCode.BadRequest
                    RpcError.InternalError -> HttpStatusCode.InternalServerError
                }
                call.respondText(result.message, status = code)
            }
            is RpcResult.Success ->  {
                call.respondBytes(result.bytes)
            }
        }
    }
}
