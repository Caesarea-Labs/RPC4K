
package io.github.natanfudge.rpc4k.runtime.api

import io.github.natanfudge.rpc4k.runtime.implementation.HeterogeneousListSerializer
import kotlinx.serialization.KSerializer


/**
 * Data representing a Remote Procedure Call.
 */
data class Rpc(val method: String, val arguments: List<*>) {
    companion object {
        private val Encoding = Charsets.UTF_8
//        fun getSerializer(argumentSerializers: List<KSerializer<*>>): KSerializer<Rpc> {
//            return serializer(HeterogeneousListSerializer(argumentSerializers)) as KSerializer<Rpc>
//        }

        fun fromByteArray(bytes: ByteArray, format: SerializationFormat, argDeserializers: List<KSerializer<*>>): Rpc {
            val (method, readBytes) = readMethodName(bytes)
            //TODO: OPTIMIZATION: if the format supports streaming, we could start off the reading from a certain index and avoid copying this large array
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
        //TODO: OPTIMIZATION: if the format supports streaming, we could write everything into one array and avoid copying these large arrays.
        return (method.toByteArray(Encoding) + ColonCode) + format.encode(HeterogeneousListSerializer(serializers), arguments)
    }


//    companion object {
//        fun peekMethodName(bytes: ByteArray): String = with(ReadContext(bytes)){
//            readMethodName()
//        }
//
//        /**
//         * See docs/rpc_format.png
//         * @param argDeserializers The callback should return the serializers of the arguments of the given method.
//         */
//        fun fromByteArray(
//            bytes: ByteArray,
//            format: SerializationFormat,
//            argDeserializers: List<DeserializationStrategy<*>>
//        ): Rpc {
//            with(ReadContext(bytes)) {
//                val methodName = readMethodName()
//
//                val args = mutableListOf<ByteArray>()
//                while (pos < bytes.size) {
//                    args.add(readArgument())
//                }
//
//                serverRequirement(bytes.size == pos) {
//                    "Incorrect argument lengths are specified. Total size of payload is ${bytes.size} when it should be $pos according to the payload itself."
//                }
//
//                serverRequirement(args.size == argDeserializers.size) {
//                    "Only ${args.size} arguments were provided when ${argDeserializers.size} are required."
//                }
//
//                return Rpc(methodName, deserializeArgs(args, format, argDeserializers))
//            }
//
//        }
//    }
}


