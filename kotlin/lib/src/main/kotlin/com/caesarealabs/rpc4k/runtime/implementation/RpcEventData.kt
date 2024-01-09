@file:Suppress("ArrayInDataClass")

package com.caesarealabs.rpc4k.runtime.implementation

import com.caesarealabs.rpc4k.runtime.api.Rpc

//TODO: add some prefix to differentiate between them
//TODO: throw error in client when error occurs
internal sealed interface RpcEventData {
    data class Emitted(val event: String, val listenerId: String, val payload: ByteArray) : RpcEventData
    data class SubscriptionError(val error: String) : RpcEventData

    fun toByteArray(): ByteArray = when (this) {
        is Emitted -> "event".toByteArray().fastConcat(Rpc.ColonCode, listenerId.toByteArray(), payload)
        is SubscriptionError -> "error".toByteArray().fastConcat(Rpc.ColonCode, error.toByteArray())
    }
}
