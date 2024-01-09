package com.caesarealabs.rpc4k.runtime.api


//TODO: document this interface
public sealed interface RpcServerEngine {
    public val eventManager: EventManager<*>

    public sealed interface SingleCall<I, O> : RpcServerEngine {
        public suspend fun read(input: I): ByteArray
        public interface Returning<I, O> : SingleCall<I, O> {
            public suspend fun respond(body: ByteArray): O
            public suspend fun respondError(message: String, errorType: RpcError): O
        }

        public interface Writing<I, O> : SingleCall<I, O> {
            public suspend fun write(body: ByteArray, output: O)
            public suspend fun writeError(message: String, errorType: RpcError, output: O)
        }
    }

    public interface MultiCall : RpcServerEngine {
        public interface Instance {
            public fun start(wait: Boolean)
            public fun stop()
        }

        public fun <RpcDef>create(setup: RpcSetupOf<RpcDef>): Instance
    }
}

//TODO: add more documentation here
/**
 * SECURITY NOTE - the message will be sent to clients. Make sure to not leak sensitive info.
 * Use this to verify inputs in service methods.
 * If verification failed the correct error will be sent to the client.
 */
public inline fun serverRequirement(condition: Boolean, message: () -> String) {
    if (!condition) {
        throw InvalidRpcRequestException(message())
    }
}

public enum class RpcError {
    InvalidRequest,
    InternalError
}

