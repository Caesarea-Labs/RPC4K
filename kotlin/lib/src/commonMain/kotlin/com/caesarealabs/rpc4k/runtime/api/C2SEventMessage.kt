package com.caesarealabs.rpc4k.runtime.api

import com.caesarealabs.rpc4k.runtime.implementation.fastConcat

public sealed interface C2SEventMessage {
    public data class Subscribe(
        public val event: String,
        /**
         * Unique identifier for the exact client function that requested this subscription. The client needs this to know where to route events.
         */
        public val listenerId: String,
        /**
         * The data by which the subscription was made. This information is exposed as parameters in the event transformer.
         */
        public val data: ByteArray,
        /**
         * @see EventTarget
         */
        public val target: String?) : com.caesarealabs.rpc4k.runtime.api.C2SEventMessage {
        override fun equals(other: Any?): Boolean {
            return other is com.caesarealabs.rpc4k.runtime.api.C2SEventMessage.Subscribe && event == other.event && listenerId == other.listenerId
                && data.contentEquals(other.data) && target == other.target
        }

        init {
            if (target != null) require(target.isNotEmpty()) { "If the target is specified it must not be blank." }
        }

        override fun hashCode(): Int {
            var result = event.hashCode()
            result = 31 * result + listenerId.hashCode()
            result = 31 * result + data.contentHashCode()
            result = 31 * result + (target?.hashCode() ?: 0)
            return result
        }
    }

    public data class Unsubscribe(val event: String, val listenerId: String) : C2SEventMessage


    public companion object {
        internal fun fromByteArray(bytes: ByteArray): C2SEventMessage {
            serverRequirement(bytes.isNotEmpty()) { "Event message is empty" }
            val reader = RpcMessageReader(bytes)
            val type = reader.readPart("type").decodeToString()
            val event = reader.readPart("event").decodeToString()
            when (type) {
                "sub" -> {
                    val listenerId = reader.readPart("listener id").decodeToString()
                    val target = reader.readPart("target").decodeToString()
                    val data = reader.readPart("payload", finalPart = true)
                    return Subscribe(
                        event,
                        listenerId,
                        data,
                        target = target.ifEmpty { null })
                }

                "unsub" -> {
                    val listenerId = reader.readPart("listener id", finalPart = true).decodeToString()
                    return Unsubscribe(event, listenerId)
                }

                else -> throw InvalidRpcRequestException("Invalid event message format, the type should be 'sub' or 'unsub' but is '${type}'")
            }
        }

    }

    public fun toByteArray(): ByteArray {
        return when (this) {
            is Subscribe -> "sub".encodeToByteArray()
                .fastConcat(Rpc.ColonCode, event.encodeToByteArray(), listenerId.encodeToByteArray(), (target ?: "").encodeToByteArray(), data)

            is Unsubscribe -> "unsub".encodeToByteArray()
                .fastConcat(Rpc.ColonCode, event.encodeToByteArray(), listenerId.encodeToByteArray())
        }
    }

}


internal class RpcMessageReader(private val bytes: ByteArray) {
    private var i = 0
    fun readPart(partName: String, finalPart: Boolean = false): ByteArray {
        if (i >= bytes.size) throw InvalidRpcRequestException("Invalid event message format, $partName is an empty string")
        val startIndex = i
        while (bytes[i] != Rpc.ColonCode || finalPart) {
            i++
            if (i >= bytes.size) {
                if (finalPart) break
                else throw InvalidRpcRequestException("Invalid event message format, missing $partName terminator ':'")
            }
        }
        // Skip the ':'
        return bytes.copyOfRange(startIndex, i++)
    }
}
