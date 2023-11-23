package io.github.natanfudge.rpc4k.runtime.api

import io.github.natanfudge.rpc4k.runtime.implementation.InvalidRpcRequestException
import io.github.natanfudge.rpc4k.runtime.implementation.serializers.HeterogeneousListSerializer
import kotlinx.serialization.KSerializer


/**
 * Data representing a Remote Procedure Call.
 */
data class Rpc(val method: String, val arguments: List<*>) {
    companion object {
        private val Encoding = Charsets.UTF_8

        fun fromByteArray(bytes: ByteArray, format: SerializationFormat, argDeserializers: List<KSerializer<*>>): Rpc {
            val (method, readBytes) = readMethodName(bytes)
            //SLOW: Copying of entire request create Rpc`s
            val arguments = format.decode(HeterogeneousListSerializer(argDeserializers), bytes.copyOfRange(readBytes, bytes.size))
            return Rpc(method, arguments)
        }

        private const val ColonCode: Byte = 58 // :

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
            return bytes.copyOfRange(0, pos - 1).toString(Encoding) to pos
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
    fun toByteArray(format: SerializationFormat, serializers: List<KSerializer<*>>): ByteArray {
        return (method.toByteArray(Encoding) + ColonCode) + format.encode(HeterogeneousListSerializer(serializers), arguments)
    }

}


