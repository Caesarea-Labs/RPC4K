
package com.caesarealabs.rpc4k.runtime.api

import com.caesarealabs.rpc4k.runtime.implementation.fastConcat

internal sealed interface S2CEventMessage {
    data class Emitted(val listenerId: String, val payload: ByteArray) : S2CEventMessage {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Emitted

            if (listenerId != other.listenerId) return false
            if (!payload.contentEquals(other.payload)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = listenerId.hashCode()
            result = 31 * result + payload.contentHashCode()
            return result
        }

    }
    data class SubscriptionError(val error: String) : S2CEventMessage

    fun toByteArray(): ByteArray = when (this) {
        is Emitted -> EmittedType.toByteArray().fastConcat(Rpc.ColonCode, listenerId.toByteArray(), payload)
        is SubscriptionError -> ErrorType.toByteArray().fastConcat(Rpc.ColonCode, error.toByteArray())
    }

    companion object {
        private const val EmittedType = "event"
        private const val ErrorType = "error"
        fun fromString(string: String): S2CEventMessage {
            val split = string.split(":")
            val type = split[0]
            when (type) {
                EmittedType -> {
                    if (split.size < 3) error("Malformed RPC event: $string")
                    val listenerId = split[1]
                    //TODO: optimize splitting and joining
                    val payload = split.drop(2).joinToString(":")
                    return Emitted(listenerId, payload.toByteArray())
                }

                ErrorType -> {
                    return SubscriptionError(split.drop(1).joinToString(":"))
                }

                else -> {
                    error("Malformed RPC message, no type specified: $string")
                }
            }
        }
    }
}
////TODO: optimize to not rejoin the message
//                const [type, listenerId, ...payload] = message.split(":")
//                switch (type) {
//                    case "event": {
//                        const listener = this.messageListeners[listenerId]
//                        if (listener !== undefined) {
//                            listener(payload.join(":"))
//                        } else {
//                            console.warn(`Could not find listener for id '${listenerId}', is the subscription still open on the server?`, message)
//                        }
//                        break
//                    }
//                    case "error": {
//                        throw new Error(`Failed to subscribe to event: ${message.removePrefix("error:")}`)
//                    }
//                }