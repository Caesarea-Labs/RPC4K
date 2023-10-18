package io.github.natanfudge.rpc4k.runtime.api

import io.github.natanfudge.rpc4k.runtime.implementation.RpcServerException
import kotlinx.serialization.KSerializer

interface RpcServer {
    suspend fun <T> send(format: SerializationFormat, response: T, serializer: KSerializer<T>)
    suspend fun sendError(message: String, errorType: RpcError)
}

/**
 * Use this to verify inputs in service methods.
 * If verification failed the correct error will be sent to the client.
 */
inline fun serverRequirement(condition: Boolean, message: () -> String) {
    if (!condition) {
        throw RpcServerException(message())
    }
}

enum class RpcError {
    InvalidRequest,
    InternalError
}

//TODO: Websocket is a problem. There's no definitive way to know what the "response" to a request is.
// However, this can be solved by providing a RequestId in each request, and then locking until a response with the same RequestId is returned.
// Also, if we're gonna go the approach if having a separate binary format for each server type, we could definitely drop the methodName header in
// http servers that allow you to do custom routing like Ktor, and keep the methodName header in cases where everything is routed to one place like in a lambda.