package io.github.natanfudge.rpc4k.runtime.impl

@JvmInline
value class CallParameters private constructor(private val bytes: ByteArray) {
    companion object {
        fun of(vararg args: ByteArray): CallParameters {
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
                val length = args.size.toUInt()
                length.write3BytesTo(resultArray, pos)
                pos += 3
                arg.copyInto(resultArray, destinationOffset = pos)
                pos += arg.size
            }

            return CallParameters(resultArray)
        }
    }

    fun get() : List<ByteArray> {
        val args = mutableListOf()
        var pos = 0
        while (pos < bytes.size) {

        }
    }

    //TODO: test this well with many random numbers, i'm not sure if the unsignedness works well


    fun combine(x: ByteArray, y: ByteArray, z: ByteArray): ByteArray {
        val xLength = x.size.toByteArray()
        val yLength = y.size.toByteArray()
        val zLength = z.size.toByteArray()

        return xLength + x + yLength + y + zLength + z
    }

    fun separate(combined: ByteArray): Triple<ByteArray, ByteArray, ByteArray> {
        var position = 0

        val xLength = combined.copyOfRange(position, position + 4).toInt()
        position += 4
        val x = combined.copyOfRange(position, position + xLength)
        position += xLength

        val yLength = combined.copyOfRange(position, position + 4).toInt()
        position += 4
        val y = combined.copyOfRange(position, position + yLength)
        position += yLength

        val zLength = combined.copyOfRange(position, position + 4).toInt()
        val z = combined.copyOfRange(position + 4, position + 4 + zLength)

        return Triple(x, y, z)
    }
}

private fun UInt.Companion.read3BytesFrom(array: ByteArray, pos: Int): UInt {
    return array[pos] + (array[pos + 1] shl 8) + (array[pos + 2])
}

/**
 * Writes the 3 least significant bytes of this UInt to [array] at the specified [pos].
 */
private fun UInt.write3BytesTo(array: ByteArray, pos: Int) {
    array[pos] = getByte(0)
    array[pos + 1] = getByte(1)
    array[pos + 2] = getByte(2)
}

/**
 * Returns the [significance]th least significant byte of this
 */
private fun UInt.getByte(significance: Int): Byte {
    return ((this shr (significance * 8)) and 0xFFu).toByte()
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

