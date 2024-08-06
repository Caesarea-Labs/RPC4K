package com.caesarealabs.rpc4k.runtime.api

import com.caesarealabs.rpc4k.runtime.implementation.fastConcat

public sealed interface S2CEventMessage {
    public data class Emitted(val listenerId: String, val payload: ByteArray) : S2CEventMessage {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Emitted) return false

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

    public data class SubscriptionError(val error: String) : S2CEventMessage

    public fun toByteArray(): ByteArray = when (this) {
        is Emitted -> EventType.encodeToByteArray().fastConcat(Rpc.ColonCode, listenerId.encodeToByteArray(), payload)
        is SubscriptionError -> ErrorType.encodeToByteArray().fastConcat(Rpc.ColonCode, error.encodeToByteArray())
    }

    public companion object {
        private const val EventType = "event"
        private const val ErrorType = "error"

        public fun fromByteArray(byteArray: ByteArray): S2CEventMessage {
            val reader = RpcMessageReader(byteArray)
            when (val type = reader.readPart("type").decodeToString()) {
                EventType -> {
                    val listenerId = reader.readPart("listenerId").decodeToString()
                    val payload = reader.readPart("payload", finalPart = true)
                    return Emitted(listenerId, payload)
                }

                ErrorType -> {
                    return SubscriptionError(reader.readPart("error", finalPart = true).decodeToString())
                }

                else -> {
                    throw InvalidRpcRequestException("Malformed RPC message, Invalid type specified: $type")
                }
            }
        }

        public fun fromString(string: String): S2CEventMessage {
            val split = string.split(":")
            val type = split[0]
            when (type) {
                EventType -> {
                    if (split.size < 3) error("Malformed RPC event: $string")
                    val listenerId = split[1]
                    // SLOW: optimize splitting and joining
                    val payload = split.drop(2).joinToString(":")
                    return Emitted(listenerId, payload.encodeToByteArray())
                }

                ErrorType -> {
                    return SubscriptionError(split.drop(1).joinToString(":"))
                }

                else -> {
                    throw InvalidRpcRequestException("Malformed RPC message, no type specified: $string")
                }
            }
        }
    }
}
