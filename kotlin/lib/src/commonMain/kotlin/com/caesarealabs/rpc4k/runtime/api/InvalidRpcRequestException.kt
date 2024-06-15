package com.caesarealabs.rpc4k.runtime.api

import com.caesarealabs.rpc4k.runtime.api.serverRequirement

/**
 * SECURITY NOTE - the message will be sent to clients. Make sure to not leak sensitive info.
 * May be thrown by an RPC server handler to signal an invalid request.
 * @see [serverRequirement]
 */
public class InvalidRpcRequestException(override val message: String, cause: Throwable? = null) : RuntimeException(message, cause)
