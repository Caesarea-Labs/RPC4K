package com.caesarealabs.rpc4k.test

import com.caesarealabs.rpc4k.runtime.api.Rpc
import com.caesarealabs.rpc4k.runtime.api.components.JsonFormat
import kotlinx.serialization.builtins.serializer
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.random.Random
import kotlin.test.Test

class EncodingTest {
    @Test
    fun `Random arrays are combined and split properly in many different cases`() {
        repeat(10) {
            validateRpcSerialization(generateRandomRpc(maxArrays = 100, maxArraySize = 10_000))
        }
    }

    @Test
    fun `Random arrays are combined and split properly`() {
        validateRpcSerialization(generateRandomRpc(minArrays = 2, maxArrays = 3, maxArraySize = 1000))
    }

    @Test
    fun `Simple rpcs are encoded and decoded properly`() {
        val rpc = Rpc("test", listOf(2, 3))
        val serializers = listOf(Int.serializer(), Int.serializer())
        val combined = rpc.toByteArray(JsonFormat(), serializers)
        val back = Rpc.fromByteArray(combined, JsonFormat(), serializers)
        expectThat(back).isEqualTo(rpc)
    }

    @Test
    fun `Very large arrays are combined and split properly`() {
        validateRpcSerialization(generateRandomRpc(minArrays = 1, maxArrays = 2, minArraySize = 4_477_215, maxArraySize = 4_777_215))
    }

    private fun generateRandomRpc(minArrays: Int = 1, maxArrays: Int, minArraySize: Int = 0, maxArraySize: Int): Rpc {
        val arrays = generateRandomByteArrays(minArrays = minArrays, maxArrays = maxArrays, minArraySize = minArraySize, maxArraySize = maxArraySize)
        return Rpc(randomValidMethodName(), arrays.map { it.decodeToString() })
    }

    private fun generateRandomByteArrays(minArrays: Int = 1, maxArrays: Int, minArraySize: Int = 0, maxArraySize: Int) =
        List(Random.nextInt(minArrays, maxArrays)) {
            Random.nextBytes(Random.nextInt(minArraySize, maxArraySize))
        }

    private fun randomValidMethodName() = List(Random.nextInt(0, 100)) { randomValidMethodNameChar() }.toCharArray().concatToString()

    private fun randomValidMethodNameChar(): Char {
        val c = (Random.nextInt(26) + 'a'.code).toChar()
        // : is in valid so if we got it, reroll.
        return if (c == ':') randomValidMethodNameChar()
        else c
    }

    private fun validateRpcSerialization(rpc: Rpc) {
        val serializers = rpc.arguments.map { String.serializer() }
        // We only use strings for testing
        val asArray = rpc.toByteArray(JsonFormat(), serializers)
        val back = Rpc.fromByteArray(asArray, JsonFormat(), serializers)
        expectThat(back).isEqualTo(back)
    }
}