package io.github.natanfudge.rpc4k.runtime.impl

import java.io.InputStream

/**
 * TODO: find a place for this comment
 * The best way is to use each format's native list construct to encode parameters.
 *  The main benefit is better debugging. Existing tools will be able to read the resulting JSON/Protobuf lists.
 *  Regarding performance, with json we don't really care, and with protobuf it should actually be slightly more performant to use their implementation.
 */

//TODO: there's a faster way to implement this. Put all of the sizes upfront and terminate with a STOP 3-bytes (0xFFF).
// This avoids iterating over the result when reading the combined value.
@JvmInline
value class CallParameters private constructor(private val bytes: ByteArray) {
    companion object {
        fun of(args: List<ByteArray>): CallParameters {
            for (arg in args) {
                //TODO: test requirement
                require(arg.size.fitsIn3Bytes()) {
                    "Length of argument of RPC call must fit in 3 bytes, but argument is extremely large (over 16 MB - ${arg.size})"
                }
            }
            //  +3 bytes for length marker before every argument
            val resultArray = ByteArray(args.sumOf { it.size + 3 })
            var pos = 0
            for (arg in args) {
                // Write the length of each arg (3 bytes), then the arg itself.
                val length = arg.size
                length.write3BytesTo(resultArray, pos)
                pos += 3
                arg.copyInto(resultArray, destinationOffset = pos)
                pos += arg.size
            }

            return CallParameters(resultArray)
        }
    }

    fun get(): List<ByteArray> {
        val args = mutableListOf<ByteArray>()
        var pos = 0
        while (pos < bytes.size) {
            // Read the length of each arg (3 bytes), then the arg itself.
            val length = Int.read3BytesFrom(bytes, pos)
            pos += 3
            args.add(bytes.copyOfRange(pos, pos + length))
            pos += length
        }
        require(bytes.size == pos)
        return args
    }
}

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

