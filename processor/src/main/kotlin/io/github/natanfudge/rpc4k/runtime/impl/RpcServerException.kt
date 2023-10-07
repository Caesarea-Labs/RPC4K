package io.github.natanfudge.rpc4k.runtime.impl

import io.github.natanfudge.rpc4k.runtime.api.RpcClientException
import io.github.natanfudge.rpc4k.runtime.api.RpcError

/**
 * Used by RPC4k to signal itself to return an error value.
 * This should not be used by users of RPC4K clients, in contrast to [RpcClientException] which is an API.
 */
@PublishedApi internal class RpcServerException(override val message: String) : RuntimeException()

internal inline fun serverRequirement(condition: Boolean, message: () -> String) {
    if (!condition) {
        throw RpcServerException(message())
    }
}