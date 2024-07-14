package com.caesarealabs.rpc4k.runtime.test

import com.caesarealabs.rpc4k.runtime.api.Rpc
import com.caesarealabs.rpc4k.runtime.implementation.fastConcat
import com.caesarealabs.rpc4k.runtime.implementation.fastSplit
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ByteOpsTest {
    @Test
    fun testConcatSplit() {
        val string = "event"
        val id = "12314145-788"
        val payload = byteArrayOf(114,12,41,56,11,-14)
        val concatenated = string.encodeToByteArray().fastConcat(Rpc.ColonCode, id.encodeToByteArray(), payload)
        val back = concatenated.fastSplit(Rpc.ColonCode)
        assertEquals(back.size, 3)
        assertContentEquals(back[0], string.encodeToByteArray())
        assertContentEquals(back[1], id.encodeToByteArray())
        assertContentEquals(payload, back[2])
    }
}