package io.github.natanfudge.rpc4k.runtime.api

import io.github.natanfudge.rpc4k.runtime.implementation.InvalidRpcRequestException


/**
 * Thrown when an RPC encountered a problem.
 * This should be caught by users of RPC4K clients, in contrast to [InvalidRpcRequestException] which is an implementation detail
 */
open class RpcClientException(message: String, val request: Rpc, val format: SerializationFormat, val client: RpcClient) : RuntimeException(message)

/**
 * Thrown when the response to an RPC indicates a problem.
 */
class RpcResponseException(message: String, request: Rpc, format: SerializationFormat, client: RpcClient, val response: String, val code: Int) :
    RpcClientException(message, request, format, client)


