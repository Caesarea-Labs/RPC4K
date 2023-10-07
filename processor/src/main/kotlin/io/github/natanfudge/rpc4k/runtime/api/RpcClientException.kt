package io.github.natanfudge.rpc4k.runtime.api
import io.github.natanfudge.rpc4k.runtime.impl.RpcServerException



/**
 * Thrown when an RPC encountered a problem.
 * This should be caught by users of RPC4K clients, in contrast to [RpcServerException] which is an implementation detail
 */
open class RpcClientException(message: String, val rpc: Rpc, val format: SerializationFormat, val transmitter: RpcClient) : RuntimeException(message)

/**
 * Thrown when the response to an RPC indicates a problem.
 */
class RpcResponseException(message: String, rpc: Rpc, format: SerializationFormat, transmitter: RpcClient, val response: String, val code: Int) :
    RpcClientException(message, rpc, format, transmitter)