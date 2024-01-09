package com.caesarealabs.rpc4k.runtime.api


/**
 * Thrown when an RPC encountered a problem.
 * This should be caught by users of RPC4K clients, in contrast to [InvalidRpcRequestException] which is an implementation detail
 */
public open class RpcClientException(message: String, public val request: Rpc, public val format: SerializationFormat, public val client: RpcClient) : RuntimeException(message)

/**
 * Thrown when the response to an RPC indicates a problem.
 */
public class RpcResponseException(message: String, request: Rpc, format: SerializationFormat, client: RpcClient, public val response: String, public val code: Int) :
    RpcClientException(message, request, format, client)


