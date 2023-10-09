package io.github.natanfudge.rpc4k.runtime.implementation.old


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

