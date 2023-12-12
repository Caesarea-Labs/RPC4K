package com.caesarealabs.rpc4k.runtime.api

import com.caesarealabs.rpc4k.runtime.implementation.InvalidRpcRequestException


sealed interface RpcServerEngine {
    sealed interface SingleCall<I, O> : RpcServerEngine {
        suspend fun read(input: I): ByteArray
        interface Returning<I, O> : SingleCall<I, O> {
            suspend fun respond(bytes: ByteArray): O
            suspend fun respondError(message: String, errorType: RpcError): O
        }

        interface Writing<I, O> : SingleCall<I, O> {
            suspend fun write(bytes: ByteArray, output: O)
            suspend fun writeError(message: String, errorType: RpcError, output: O)
        }
    }

    interface MultiCall : RpcServerEngine {
        interface Instance {
            fun start(wait: Boolean)
            fun stop()
        }

        fun create(setup: RpcServerSetup<*, *>): Instance
    }
}


/**
 * SECURITY NOTE - the message will be sent to clients. Make sure to not leak sensitive info.
 * Use this to verify inputs in service methods.
 * If verification failed the correct error will be sent to the client.
 */
inline fun serverRequirement(condition: Boolean, message: () -> String) {
    if (!condition) {
        throw InvalidRpcRequestException(message())
    }
}

enum class RpcError {
    InvalidRequest,
    InternalError
}

