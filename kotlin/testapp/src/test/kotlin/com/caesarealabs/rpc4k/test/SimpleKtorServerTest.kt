package com.caesarealabs.rpc4k.test

import com.caesarealabs.rpc4k.generated.SimpleProtocolClientImpl
import com.caesarealabs.rpc4k.generated.SimpleProtocolEventInvoker
import com.caesarealabs.rpc4k.testapp.SimpleProtocol
import com.caesarealabs.rpc4k.runtime.api.testing.rpcExtension
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.test.Test

class SimpleKtorServerTest {
    companion object {
        @JvmField
        @RegisterExtension
        val extension = rpcExtension<SimpleProtocol, SimpleProtocolEventInvoker, SimpleProtocolClientImpl>({ SimpleProtocol() })
    }


    @Test
    fun testManual(): Unit = runBlocking {
        val response = extension.client.bar(2)
        assertEquals(3, response)
    }


}