package io.github.natanfudge.rpc4k.runtime.implementation

import io.github.natanfudge.rpc4k.runtime.api.Rpc
import io.github.natanfudge.rpc4k.runtime.api.SerializationFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy

private val Encoding = Charsets.UTF_8
private const val ColonCode: Byte = 58 // :

internal fun Rpc.calculatePayloadSize(argBytes: List<ByteArray>): Int {
    val lengthsSize = argBytes.size * 3
    // + 1 for ':'
    val methodNameSize = method.length + 1
    val argsSize = argBytes.sumOf { it.size }
    return lengthsSize + methodNameSize + argsSize
}

/**
 * Checks all [argBytes] fit in 3 bytes
 */
internal fun validateArgSizes(argBytes: List<ByteArray>) {
    for (arg in argBytes) {
        val size = arg.size

        require(size.fitsIn3Bytes()) {
            "Length of argument of RPC call must fit in 3 bytes, but argument is extremely large (over 16 MB - ${arg.size})"
        }
    }
}


internal fun serializeArgs(args: List<*>, format: SerializationFormat, argDeserializers: List<SerializationStrategy<*>>): List<ByteArray> {
    return args.zip(argDeserializers).map { (arg, serializer) ->
        // We trust the correct serializers are provided
        @Suppress("UNCHECKED_CAST")
        format.encode(serializer as KSerializer<Any?>, arg)
    }
}


internal class WriteContext(private val result: ByteArray) {
    var pos = 0

    internal fun WriteContext.writeMethod(method: String) {
        // Write method name
        writeMethodName(method.toByteArray(Encoding))
        // Write ':'
        writeMethodNameSeparator()
    }

    private fun WriteContext.writeMethodName(methodBytes: ByteArray) {
        methodBytes.copyInto(result, pos)
        pos += methodBytes.size
    }

    private fun WriteContext.writeMethodNameSeparator() {
        result[pos] = ColonCode
        pos++
    }

    internal fun WriteContext.writeArgument(arg: ByteArray, resultArray: ByteArray) {
        // Write the length of each arg (3 bytes)
        writeArgumentLength(arg, resultArray)
        // Write each arg (variable bytes
        writeArgumentContents(arg, resultArray)
    }

    private fun WriteContext.writeArgumentLength(arg: ByteArray, resultArray: ByteArray) {
        arg.size.write3BytesTo(resultArray, pos)
        pos += 3
    }

    private fun WriteContext.writeArgumentContents(arg: ByteArray, resultArray: ByteArray) {
        arg.copyInto(resultArray, destinationOffset = pos)
        pos += arg.size
    }

}


internal class ReadContext(private val bytes: ByteArray) {
    var pos = 0

    internal fun readMethodName(): String {
        // Reads up until COLON_CODE,
        do {
            val currentByte = bytes[pos]
            // Happens after array[pos] so we will already skip by the color itself
            pos++
        } while (currentByte != ColonCode)

        // Exclude colon itself
        return bytes.copyOfRange(0, pos - 1).toString(Encoding)
    }

    internal fun readArgument(): ByteArray {
        // Read the length of each arg (3 bytes), then the arg itself.
        val length = readArgumentLength()
        return readArgumentContents(length)
    }

    private fun readArgumentContents(length: Int): ByteArray {
        val contents = bytes.copyOfRange(pos, pos + length)
        pos += length
        return contents
    }

    private fun readArgumentLength(): Int {
        val length = Int.read3BytesFrom(bytes, pos)
        pos += 3
        return length
    }

    internal fun deserializeArgs(args: List<ByteArray>, format: SerializationFormat, argDeserializers: List<DeserializationStrategy<*>>): List<*> {
        return args.zip(argDeserializers).map { (arg, deserializer) -> format.decode(deserializer, arg) }
    }
}





