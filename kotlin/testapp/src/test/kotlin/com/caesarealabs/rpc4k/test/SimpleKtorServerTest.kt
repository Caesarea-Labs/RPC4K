package com.caesarealabs.rpc4k.test

import com.caesarealabs.rpc4k.generated.SimpleProtocolEventInvoker
import com.caesarealabs.rpc4k.generated.rpc4k
import com.caesarealabs.rpc4k.runtime.api.testing.junit
import com.caesarealabs.rpc4k.testapp.SimpleProtocol
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.test.Test

class SimpleKtorServerTest {
    companion object {
        @JvmField
        @RegisterExtension
        val extension = SimpleProtocol.rpc4k.junit { SimpleProtocol() } 
    }


    @Test
    fun testManual(): Unit = runBlocking {
        val response = extension.client.bar(2)
        assertEquals(3, response)
    }


}