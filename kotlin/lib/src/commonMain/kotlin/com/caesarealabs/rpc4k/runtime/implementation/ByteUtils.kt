package com.caesarealabs.rpc4k.runtime.implementation



public fun ByteArray.fastConcat(separator: Byte, vararg others: ByteArray): ByteArray {
    // Count in separator as well
    val res = ByteArray(this.size + others.sumOf { it.size + 1 })
    this.copyInto(res)
    var currentPosition = this.size
    for(other in others) {
        res[currentPosition] = separator
        currentPosition++
        other.copyInto(res, currentPosition)
        currentPosition += other.size
    }
    return res
}