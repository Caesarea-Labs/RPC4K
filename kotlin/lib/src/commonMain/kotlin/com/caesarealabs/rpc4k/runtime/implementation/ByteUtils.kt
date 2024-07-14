package com.caesarealabs.rpc4k.runtime.implementation


/**
 * Efficiently join a [ByteArray] with other [ByteArray]s, spacing by the given [separator]
 */
internal fun ByteArray.fastConcat(separator: Byte, vararg others: ByteArray): ByteArray {
    // Count in separator as well
    val res = ByteArray(this.size + others.sumOf { it.size + 1 })
    this.copyInto(res)
    var currentPosition = this.size
    for (other in others) {
        res[currentPosition] = separator
        currentPosition++
        other.copyInto(res, currentPosition)
        currentPosition += other.size
    }
    return res
}

/**
 * Efficiently splits a [ByteArray] into parts seperated by [separator].
 * @param separator Must not be 0.
 */
internal fun ByteArray.fastSplit(separator: Byte): List<ByteArray> {
    check(isNotEmpty())
    val res = mutableListOf<ByteArray>()
    var currentPartStart = 0
    var i = 0
    while (i < size) {
        var current = 0.toByte()
        // Read current part
        while (current != separator && i < size) {
            current = this[i]
            i++
        }
        // Exclude separator if stopped at separator. If stopped at end there's no need to exclude it.
        val end = if (current == separator) i - 1 else i
        // Separator reached - reset and add part
        val part = ByteArray(end - currentPartStart)
        copyInto(part, startIndex = currentPartStart, endIndex = end)

        res.add(part)
        currentPartStart = i

    }

    return res

}