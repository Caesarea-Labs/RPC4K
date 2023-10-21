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

