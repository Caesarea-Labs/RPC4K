package com.caesarealabs.rpc4k.runtime.api



/**
 * Specifies how RPC event messages should be sent
 */
public interface RpcMessageLauncher {
    /**
     * Returns true if the message was reached.
     *
     * Return false if the target of the connection is gone, and the connection should be removed
     */
    public suspend fun send(connection: EventConnection, bytes: ByteArray): Boolean
}

/**
 * A server platform that handles routing RPC requests itself, using the specified config in [start].
 * Once the server is not needed, [stop] should be called.
 */
public interface DedicatedServer: RpcMessageLauncher {
    public fun start(config: ServerConfig, wait: Boolean = true)
    public fun stop()
}

/**
 * If the condition is not true, the current RPC call will return and error signaling that there is some problem with the input of the client.
 *
 * **SECURITY NOTE** - the message will be sent to clients. Make sure to not leak sensitive info.
 * Use this to verify inputs in service methods.
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

