package io.github.natanfudge.rpc4k.runtime.api.components

import io.github.natanfudge.rpc4k.runtime.api.RpcError
import io.github.natanfudge.rpc4k.runtime.api.RpcError.*
import io.github.natanfudge.rpc4k.runtime.api.RpcServer
import io.github.natanfudge.rpc4k.runtime.api.SerializationFormat
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.KSerializer

//TODO: add utility for registering a route that uses KtorRpcServer
class KtorSingleRouteRpcServer(private val call: ApplicationCall) : RpcServer {
    override suspend fun <T> send(format: SerializationFormat, response: T, serializer: KSerializer<T>) {
        call.respondBytes(format.encode(serializer, response))
    }

    override suspend fun sendError(message: String, errorType: RpcError) {
        call.respondText(message, status = when(errorType) {
            InvalidRequest -> HttpStatusCode.BadRequest
            InternalError -> HttpStatusCode.InternalServerError
        })
    }
}