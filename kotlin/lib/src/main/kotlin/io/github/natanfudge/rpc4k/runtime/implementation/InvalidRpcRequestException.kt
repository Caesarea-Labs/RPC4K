package io.github.natanfudge.rpc4k.runtime.implementation

import io.github.natanfudge.rpc4k.runtime.api.serverRequirement

/**
 * SECURITY NOTE - the message will be sent to clients. Make sure to not leak sensitive info.
 * May be thrown by an RPC server handler to signal an invalid request.
 * @see [serverRequirement]
 */
 class InvalidRpcRequestException(override val message: String, cause: Throwable? = null) : RuntimeException(message,cause)
