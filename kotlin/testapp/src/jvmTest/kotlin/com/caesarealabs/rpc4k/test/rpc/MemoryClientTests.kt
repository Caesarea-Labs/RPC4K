package com.caesarealabs.rpc4k.test.rpc

import com.caesarealabs.rpc4k.generated.rpc4k
import com.caesarealabs.rpc4k.runtime.user.components.MemoryDedicatedServer
import com.caesarealabs.rpc4k.runtime.user.components.MemoryRpcClient
import com.caesarealabs.rpc4k.runtime.jvm.user.testing.junit
import com.caesarealabs.rpc4k.testapp.AllEncompassingService
import com.caesarealabs.rpc4k.testapp.SimpleProtocol
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.test.Test

class MemoryClientTests {
    companion object {
        @JvmField
        @RegisterExtension
        val allEncompassingExtension = AllEncompassingService.rpc4k.junit(server = { MemoryDedicatedServer(it) }, client = { port ,_, _ ->
            MemoryRpcClient(port)
        }) { AllEncompassingService(it) }

        @JvmField
        @RegisterExtension
        val simpleExtension = SimpleProtocol.rpc4k.junit(server = { MemoryDedicatedServer(it) }, client = { port ,_, _ ->
            MemoryRpcClient(port)
        }) { SimpleProtocol() }
    }



    @Test
    fun testGeneratedEvents() {
        RpcTests.testGeneratedEvents(allEncompassingExtension)
    }

    @Test
    fun testUsage() {
        RpcTests.testUsage(allEncompassingExtension)
    }

    @Test
    fun testNullableTypes() {
        RpcTests.testNullableTypes(allEncompassingExtension)
    }

    @Test
    fun testManual() {
        RpcTests.simpleClassTest(simpleExtension)
    }

    @Test
    fun testExceptions() {
        RpcTests.testExceptions(allEncompassingExtension)
    }

    @Test
    fun testExoticTypes() {
        RpcTests.testExoticTypes(allEncompassingExtension)
    }

    @Test
    fun testParticipants(): Unit = runBlocking {
        RpcTests.testParticipants(allEncompassingExtension)
    }

}