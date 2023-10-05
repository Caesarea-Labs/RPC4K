package io.github.natanfudge.rpc4k

import io.github.natanfudge.rpc4k.runtime.impl.CallParameters
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import kotlin.random.Random
import kotlin.test.Test

class EncodingTest {

    private fun generateRandomByteArrays(minArrays: Int = 1, maxArrays: Int, maxArraySize: Int) = List(Random.nextInt(minArrays, maxArrays)) {
        //TODO: see if putting higher number lags tests
        Random.nextBytes(Random.nextInt(0, maxArraySize))
    }

    @Test
    fun `Random arrays are combined and split properly in many different cases`() {
        repeat(10) {
            val arrays = generateRandomByteArrays(maxArrays = 100, maxArraySize = 10_000)
            val combined = CallParameters.of(arrays)
            val back = combined.get()
            arrays.expectEquals(back)
        }
    }

    @Test
    fun `Random arrays are combined and split properly`() {
        val arrays = generateRandomByteArrays(minArrays = 2, maxArrays = 3, maxArraySize = 1000)
        val combined = CallParameters.of(arrays)
        val back = combined.get()
        arrays.expectEquals(back)
    }

    @Test
    fun `Simple arrays are combined and split properly`() {
        val arrays = listOf(byteArrayOf(0), byteArrayOf(1))

        val combined = CallParameters.of(arrays)
        val back = combined.get()
        arrays.expectEquals(back)
    }

    @Test
    fun `Very large arrays are combined and split properly`() {
        val array1 = ByteArray(16_777_215)
        val array2 = ByteArray(16_477_215)

        array1[16_777_214] = 8
        array1[16_777_212] = 3

        array2[16_377_214] = 8
        array2[16_177_212] = 3

        val arrays = listOf(array1, array2)

        val combined = CallParameters.of(arrays)
        val back = combined.get()
        arrays.expectEquals(back)
    }

    @Test
    fun `Too large arrays are not allowed`() {
        val arrays = listOf(ByteArray(18_777_215))
        expectThrows<IllegalArgumentException> {
            CallParameters.of(arrays)
        }
    }

    private fun List<ByteArray>.expectEquals(other: List<ByteArray>) = expectThat(map { it.toList() })
        .isEqualTo(other.map { it.toList() })
}