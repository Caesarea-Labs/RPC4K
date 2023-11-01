package io.github.natanfudge.rpc4k.runtime.implementation


internal fun Int.Companion.read3BytesFrom(array: ByteArray, pos: Int): Int {
    return array[pos].withSignificance(0) + array[pos + 1].withSignificance(1) + array[pos + 2].withSignificance(2)
}

/**
 * Writes the 3 least significant bytes of this UInt to [array] at the specified [pos].
 */
internal fun Int.write3BytesTo(array: ByteArray, pos: Int) {
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

internal fun Int.fitsIn3Bytes() = this <= 16_777_215
