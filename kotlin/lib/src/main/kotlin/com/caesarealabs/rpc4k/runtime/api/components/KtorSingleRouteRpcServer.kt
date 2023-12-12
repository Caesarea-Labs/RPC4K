package com.caesarealabs.rpc4k.runtime.api.components

import com.caesarealabs.rpc4k.runtime.api.RpcError
import com.caesarealabs.rpc4k.runtime.api.RpcServerEngine
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.utils.io.core.*

class KtorSingleRouteRpcServer : RpcServerEngine.SingleCall.Writing<ApplicationCall, ApplicationCall> {
    override suspend fun read(input: ApplicationCall): ByteArray {
        return input.receiveChannel().readRemaining().readBytes()
    }

    override suspend fun write(bytes: ByteArray, output: ApplicationCall) {
        output.respondBytes(bytes)
    }

    override suspend fun writeError(message: String, errorType: RpcError, output: ApplicationCall) {
        val code = when (errorType) {
            RpcError.InvalidRequest -> HttpStatusCode.BadRequest
            RpcError.InternalError -> HttpStatusCode.InternalServerError
        }
        output.respondText(message, status = code)
    }
}