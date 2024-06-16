package com.caesarealabs.rpc4k.runtime.api

import com.caesarealabs.rpc4k.runtime.implementation.fastConcat
import com.caesarealabs.rpc4k.runtime.implementation.serializers.TupleSerializer
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.KSerializer


/**
 * Data representing a Remote Procedure Call.
 */
public data class Rpc(val method: String, val arguments: List<*>) {
    internal companion object {
//        private val Encoding = Charsets.UTF_8

        fun fromByteArray(bytes: ByteArray, format: SerializationFormat, argDeserializers: List<KSerializer<*>>): Rpc {
            val (method, readBytes) = readMethodName(bytes)
            //SLOW: Copying of entire request create Rpc`s
            val arguments = format.decode(TupleSerializer(argDeserializers), bytes.copyOfRange(readBytes, bytes.size))
            return Rpc(method, arguments)
        }

         const val ColonCode: Byte = 58 // :

        fun peekMethodName(rpcBytes: ByteArray): String {
            return readMethodName(rpcBytes).first
        }

        /**
         * Returns the method name and how many bytes it spans at the start of the [bytes].
         */
        private fun readMethodName(bytes: ByteArray): Pair<String, Int> {
            var pos = 0
            // Reads up until COLON_CODE,
            do {
                if (pos == bytes.size) {
                    throw InvalidRpcRequestException("Missing colon (:) separator in RPC: '${bytes.decodeToString()}'")
                }
                val currentByte = bytes[pos]
                // Happens after array[pos] so we will already skip by the color itself
                pos++
            } while (currentByte != ColonCode)

            // Exclude colon itself
            return bytes.copyOfRange(0, pos - 1).decodeToString() to pos
        }
    }

    init {
        check(!method.contains(':')) { "Method name must not contain ':', but it did: \"$method\"" }
    }

    override fun toString(): String {
        return "$method(${arguments.joinToString()})"
    }


    /**
     * See docs/rpc_format.png
     */
    public fun toByteArray(format: SerializationFormat, serializers: List<KSerializer<*>>): ByteArray {
        return method.encodeToByteArray().fastConcat(ColonCode, format.encode(TupleSerializer(serializers), arguments))
    }

}


