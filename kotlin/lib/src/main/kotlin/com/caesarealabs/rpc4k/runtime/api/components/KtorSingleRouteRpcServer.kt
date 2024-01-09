package com.caesarealabs.rpc4k.runtime.api.components

import com.caesarealabs.rpc4k.runtime.api.EventManager
import com.caesarealabs.rpc4k.runtime.api.RpcError
import com.caesarealabs.rpc4k.runtime.api.RpcServerEngine
import com.caesarealabs.rpc4k.runtime.implementation.KtorEventManager
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.utils.io.core.*

public class KtorSingleRouteRpcServer : RpcServerEngine.SingleCall.Writing<ApplicationCall, ApplicationCall> {
    override val eventManager: EventManager<*> = KtorEventManager()
    override suspend fun read(input: ApplicationCall): ByteArray {
        return input.receiveChannel().readRemaining().readBytes()
    }

    override suspend fun write(body: ByteArray, output: ApplicationCall) {
        output.respondBytes(body)
    }

    override suspend fun writeError(message: String, errorType: RpcError, output: ApplicationCall) {
        val code = when (errorType) {
            RpcError.InvalidRequest -> HttpStatusCode.BadRequest
            RpcError.InternalError -> HttpStatusCode.InternalServerError
        }
        output.respondText(message, status = code)
    }
}