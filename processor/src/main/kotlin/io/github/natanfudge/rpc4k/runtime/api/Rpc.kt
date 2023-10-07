package io.github.natanfudge.rpc4k.runtime.api

import io.github.natanfudge.rpc4k.runtime.impl.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy


/**
 * Data representing a Remote Procedure Call.
 */
data class Rpc(val method: String, val arguments: List<*>) {
    init {
        check(!method.contains(':')) { "Method name must not contain ':', but it did: \"$method\"" }
    }

    override fun toString(): String {
        return "$method(${arguments.joinToString()})"
    }


    /**
     * See docs/rpc_format.png
     */
    fun toByteArray(format: SerializationFormat, serializers: List<SerializationStrategy<*>>): ByteArray {
        //TODO: OPTIMIZATION: for JVM only, and for formats supporting writing to a stream only,
        // it would be faster to create a ByteArrayOutputStream and write to it directly
        // instead of creating ByteArrays for every argument and then copying those ByteArrays:
        // 1. Create BAOS
        // 2. Write methodName to BAOS
        // 3. Write length of first param to BAOS
        // 4. Write first param to BAOS using format stream encoding directly
        // 5. Repeat for all params
        // 6. Extend BAOS to directly get the written bytes instead of copying them

        val argBytes = serializeArgs(arguments, format, serializers)
        validateArgSizes(argBytes)

        val resultArray = ByteArray(calculatePayloadSize(argBytes))
        with(WriteContext(resultArray)) {
            writeMethod(method)

            // Write args
            for (arg in argBytes) {
                writeArgument(arg, resultArray)
            }

            check(resultArray.size == pos) { "Sanity check to see the array we allocated is of the exact correct size" }
        }

        return resultArray
    }


    companion object {
        fun peekMethodName(bytes: ByteArray): String = with(ReadContext(bytes)){
            readMethodName()
        }

        /**
         * See docs/rpc_format.png
         * @param argDeserializers The callback should return the serializers of the arguments of the given method.
         */
        fun fromByteArray(
            bytes: ByteArray,
            format: SerializationFormat,
            argDeserializers: List<DeserializationStrategy<*>>
        ): Rpc {
            with(ReadContext(bytes)) {
                val methodName = readMethodName()

                val args = mutableListOf<ByteArray>()
                while (pos < bytes.size) {
                    args.add(readArgument())
                }

                serverRequirement(bytes.size == pos) {
                    "Incorrect argument lengths are specified. Total size of payload is ${bytes.size} when it should be $pos according to the payload itself."
                }

                serverRequirement(args.size == argDeserializers.size) {
                    "Only ${args.size} arguments were provided when ${argDeserializers.size} are required."
                }

                return Rpc(methodName, deserializeArgs(args, format, argDeserializers))
            }

        }
    }
}


