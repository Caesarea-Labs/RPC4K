package io.github.natanfudge.rpc4k.runtime.impl

import io.github.natanfudge.rpc4k.runtime.api.format.SerializationFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
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
        val argBytes = serializeArgs(arguments, format, serializers)
        validateArgSizes(argBytes)

        val methodBytes = method.toByteArray(encoding)
        val lengthsSize = argBytes.size * 3
        // + 1 for ':'
        val methodNameSize = method.length + 1
        val argsSize = argBytes.sumOf { it.size }
        val resultArray = ByteArray(lengthsSize + methodNameSize + argsSize)
        var pos = 0

        // Write method name
        methodBytes.copyInto(resultArray, pos)
        pos += methodBytes.size
        // Write ':'
        resultArray[pos] = COLON_CODE
        pos++

        // Write args
        for (arg in argBytes) {
            // Write the length of each arg (3 bytes)
            val length = arg.size
            length.write3BytesTo(resultArray, pos)
            pos += 3
            // Write each arg (variable bytes
            arg.copyInto(resultArray, destinationOffset = pos)
            pos += arg.size
        }

        check(resultArray.size == pos) { "Sanity check to see the array we allocated is of the exact correct size" }

        return resultArray
    }

    private fun validateArgSizes(argBytes: List<ByteArray>) {
        for (arg in argBytes) {
            val size = arg.size

            require(size.fitsIn3Bytes()) {
                "Length of argument of RPC call must fit in 3 bytes, but argument is extremely large (over 16 MB - ${arg.size})"
            }
        }
    }


    companion object {
        private val encoding = Charsets.UTF_8
        private const val COLON_CODE: Byte = 58 // :

        /**
         * See docs/rpc_format.png
         */
        fun fromByteArray(bytes: ByteArray, format: SerializationFormat, argDeserializers: List<DeserializationStrategy<*>>): Rpc {
            val (methodName, startingPos) = readMethodName(bytes)
            var pos = startingPos

            val args = mutableListOf<ByteArray>()
            while (pos < bytes.size) {
                // Read the length of each arg (3 bytes), then the arg itself.
                val length = Int.read3BytesFrom(bytes, pos)
                pos += 3
                args.add(bytes.copyOfRange(pos, pos + length))
                pos += length
            }
            //TODO: test requirement
            require(bytes.size == pos) {
                "Incorrect argument lengths are specified. Total size of payload is ${bytes.size} when it should be $pos according to the payload itself."
            }
            //TODO: test requirement
            require(args.size == argDeserializers.size) {
                "Only ${args.size} arguments were provided when ${argDeserializers.size} are required."
            }

            return Rpc(methodName, deserializeArgs(args, format, argDeserializers))
        }

        /**
         * Returns the method name and the amount of bytes that has been read
         */
        private fun readMethodName(array: ByteArray): Pair<String, Int> {
            var pos = 0
            // Reads up until COLON_CODE,
            do {
                val currentByte = array[pos]
                // Happens after array[pos] so we will already skip by the color itself
                pos++
            } while (currentByte != COLON_CODE)

            // Exclude colon itself
            return array.copyOfRange(0, pos - 1).toString(encoding) to pos
        }

        private fun serializeArgs(args: List<*>, format: SerializationFormat, argDeserializers: List<SerializationStrategy<*>>): List<ByteArray> {
            return args.zip(argDeserializers).map { (arg, serializer) ->
                // We trust the correct serializers are provided
                @Suppress("UNCHECKED_CAST")
                format.encode(serializer as KSerializer<Any?>, arg)
            }
        }

        private fun deserializeArgs(args: List<ByteArray>, format: SerializationFormat, argDeserializers: List<DeserializationStrategy<*>>): List<*> {
            return args.zip(argDeserializers).map { (arg, deserializer) -> format.decode(deserializer, arg) }
        }
    }
//        fun of(method: String, args: List<ByteArray>): Rpc {
//            for (arg in args) {
//                val size = arg.size
//
//                require(size.fitsIn3Bytes() && size != LENGTHS_END) {
//                    "Length of argument of RPC call must fit in 3 bytes, but argument is extremely large (over 16 MB - ${arg.size})"
//                }
//            }
//            //  +3 bytes for length marker before every argument
//            val resultArray = ByteArray(args.sumOf { it.size + 3 })
//            var pos = 0
//            for (arg in args) {
//                // Write the length of each arg (3 bytes), then the arg itself.
//                val length = arg.size
//                length.write3BytesTo(resultArray, pos)
//                pos += 3
//                arg.copyInto(resultArray, destinationOffset = pos)
//                pos += arg.size
//            }
//
//            return Rpc(resultArray)
//        }
//    }
//
//    fun get(): List<ByteArray> {
//        val args = mutableListOf<ByteArray>()
//        var pos = 0
//        while (pos < bytes.size) {
//            // Read the length of each arg (3 bytes), then the arg itself.
//            val length = Int.read3BytesFrom(bytes, pos)
//            pos += 3
//            args.add(bytes.copyOfRange(pos, pos + length))
//            pos += length
//        }
//        require(bytes.size == pos)
//        return args
//    }
}

//data class Argument<T>(val value: T, val serializer: SerializationStrategy<T>) {
//    override fun toString(): String = value.toString()
//
//    // This utility method helps avoid unsafe casts
//    fun encode(format: SerializationFormat) = format.encode(serializer, value)
//}

private fun Int.Companion.read3BytesFrom(array: ByteArray, pos: Int): Int {
    return array[pos].withSignificance(0) + array[pos + 1].withSignificance(1) + array[pos + 2].withSignificance(2)
}
//TODO: test this well with many random numbers, i'm not sure if the unsignedness works well
/**
 * Writes the 3 least significant bytes of this UInt to [array] at the specified [pos].
 */
private fun Int.write3BytesTo(array: ByteArray, pos: Int) {
    array[pos] = getByte(0)
    array[pos + 1] = getByte(1)
    array[pos + 2] = getByte(2)
}

/**
 * Returns the [significance]th least significant byte of this
 */
private fun Int.getByte(significance: Int): Byte {
    return ((this shr (significance * 8)) and 0xFF).toByte()
}

private fun Byte.withSignificance(significance: Int): Int {
    // Important: treat this byte as an unsigned byte because we are encoding sizes here
    return (this.toUByte().toInt() shl (significance * 8))
}

private fun Int.fitsIn3Bytes() = this <= 16_777_215

/////////////////////////////////////// OLD ////////////////////////////
internal fun List<ByteArray>.encodeAndJoin(): ByteArray = map { it.encodeToJoinable() }.joinJoinable(JoinByte)
internal fun ByteArray.splitJoinedAndDecode(): List<ByteArray> = splitJoinable().map { it.decodeJoinable() }

private const val devChecks = true

private inline fun devCheck(check: () -> Boolean) {
    if (devChecks && !check()) throw IllegalStateException()
}

private const val JoinByte = 0x01.toByte()
private const val EncodingByte = 0x00.toByte()

/**
 * We encode the bytearray so we can reliably combine it with a SplitChar and split it.
 *
 * 0x00 -> 0x00_00
 * 0x01 -> 0x00_01
 * JoinByte -> 0x01
 */
private fun ByteArray.encodeToJoinable(): ByteArray {
    // 0x00 and 0x01 are twice as long
    val newSize = size + count { it == EncodingByte || it == JoinByte }
    val newArray = ByteArray(newSize)

    var currentIndex = 0
    for (byte in this) {
        when (byte) {
            EncodingByte, JoinByte -> {
                newArray[currentIndex++] = EncodingByte
            }
        }
        newArray[currentIndex++] = byte
    }
    return newArray
}

/**
 * The elements in the list are concatenated with a JoinByte after each element.
 * [0x030507,0x071112] -> 0x030507 JoinByte 0x071112 JoinByte
 */
private fun List<ByteArray>.joinJoinable(separator: Byte): ByteArray {
    if (isEmpty()) return ByteArray(0)
    val separators = this.size
    val newArray = ByteArray(this.sumOf { it.size } + separators)
    var currentIndex = 0
    this.forEach { array ->
        for (byte in array) {
            newArray[currentIndex] = byte
            currentIndex++
        }
        newArray[currentIndex] = separator
        currentIndex++
    }
    return newArray
}

private fun ByteArray.splitJoinable(): List<ByteArray> {
    if (isEmpty()) return listOf()
    val list = mutableListOf<ByteArray>()
    var currentArrayStart = 0
    var encodingByte = false
    for (i in indices) {
        val byte = this[i]
        // If encodingByte is true it means this byte is LITERALLY 0x00 or 0x01, and not the JoinByte or the EncodingByte.
        if (byte == JoinByte && !encodingByte) {
            val part = copyOfRangeOrEmpty(currentArrayStart, i)
            list.add(part)
            currentArrayStart = i + 1
        }

        if (encodingByte) {
            encodingByte = false
        } else if (byte == EncodingByte) {
            encodingByte = true
        }
    }

    return list
}

private fun ByteArray.decodeJoinable(): ByteArray {
    val originalArray = ByteArray(size - getIncreasedSizeOfJoinable())
    var encodingByte = false
    var originArrayIndex = 0
    for (i in indices) {
        val byte = this[i]
        if (encodingByte) {
            devCheck {
                byte == EncodingByte || byte == JoinByte
            }
            // 0x00_00 or 0x00_01
            originalArray[originArrayIndex++] = byte
            encodingByte = false
        } else if (byte == EncodingByte) {
            // 0x00, will wait for next byte
            encodingByte = true
        } else {
            // 0x02 and up, encoded normally
            originalArray[originArrayIndex++] = byte
        }
    }
    return originalArray
}

private fun ByteArray.getIncreasedSizeOfJoinable(): Int {
    var size = 0
    var encodingByte = false
    for (byte in this) {
        if (encodingByte) {
            size++
            encodingByte = false
        } else if (byte == EncodingByte) {
            encodingByte = true
        }
    }
    return size
}

private fun ByteArray.copyOfRangeOrEmpty(startIndex: Int, endIndex: Int) =
    if (startIndex >= 0 && startIndex < endIndex && endIndex <= size) copyOfRange(startIndex, endIndex)
    else ByteArray(0)

