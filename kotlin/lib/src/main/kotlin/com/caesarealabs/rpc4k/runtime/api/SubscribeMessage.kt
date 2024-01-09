@file:Suppress("ArrayInDataClass")

package com.caesarealabs.rpc4k.runtime.api

public sealed interface EventMessage {
    public data class Subscribe(
        val event: String,
        /**
         * Unique identifier for the exact client function that requested this subscription. The client needs this to know where to route events.
         */
        val listenerId: String,
        /**
         * The data by which the subscription was made. This information is exposed as parameters in the event transformer.
         */
        val data: ByteArray,
        /**
         * This is a performance precaution to make invoking events faster.
         * Consider Google had a Google Sheets event called 'sheet_changed'.
         * If for every change in any sheet, all sheets would need to be checked in the event transformer, that would be extremely slow.
         * However, if a singular sheet would receive a unique 'sheet-id', then whenever the sheet changes only subscriptions to the same
         * sheet would be considered and it would be very efficient.
         * We can see, that for any event there should be some way to focus the events onto some specific object.
         */
        val watchedObjectId: String?) : EventMessage

    public data class Unsubscribe(val event: String, val listenerId: String) : EventMessage


    public companion object {
//        fun fromWebsocketMessage(message: String): EventMessage {
//
//        }

        //TODO: test this together with toByteArray
        internal fun fromByteArray(bytes: ByteArray): EventMessage {
            serverRequirement(bytes.isNotEmpty()) { "Event message is empty" }
            val reader = MessageReader(bytes)
            val type = reader.readPart("type").decodeToString()
            val event = reader.readPart("event").decodeToString()
            when (type) {
                "sub" -> {
                    val listenerId = reader.readPart("listener id").decodeToString()
                    val watchedObjectId = reader.readPart("watched object id").decodeToString()
                    val data = reader.readPart("payload", finalPart = true)
                    return Subscribe(event, listenerId, data, watchedObjectId = watchedObjectId.ifEmpty { null })
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
        TODO()
    }

}


private class MessageReader(private val bytes: ByteArray) {
    private var i = 0
    fun readPart(partName: String, finalPart: Boolean = false): ByteArray {
        val startIndex = i
        while (bytes[i] != Rpc.ColonCode) {
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
//
///**
// * AWS provides a way to handle websocket messages in a serverless manner.
// * Unfortunately, it works by accepting all messages as a json, reading the 'action' field and distributing to the correct
// * serverless function that maps to the value of the action.
// * For example:
// * action: dogMessage -> Lambda Function 1
// * action: catMessage -> Lambda Function 2
// * etc...
// *
// *
// */
//@Serializable
//private data class WebsocketMessage(
//    /**
//     * The action is ignored, since in AWS it's always 'sendMessage'.
//     */
//    val action: String,
//    val message: String
//)