package com.caesarealabs.rpc4k.runtime.implementation


//fun ByteArray.fastConcat(separator: Byte, array: ByteArray): ByteArray {
//    val res = ByteArray(this.size + 1 + array.size)
//    this.copyInto(res)
//    res[this.size] = separator
//    array.copyInto(res, this.size + 1)
//    return res
//}

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