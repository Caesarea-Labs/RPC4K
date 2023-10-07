package io.github.natanfudge.rpc4k_test

import com.example.CreateLobbyResponse
import com.example.PlayerId
import com.example.SimpleProtocol
import com.example.UserProtocol
import io.github.natanfudge.rpc4k.runtime.api.old.client.JvmProtocolFactory
import io.github.natanfudge.rpc4k.runtime.api.old.server.RpcServer
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import org.junit.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class FakeTest {
    init {
        println("FakeTest started at ${System.currentTimeMillis()}")
    }

    @Test
    fun testUsage() = serverTest {
        RpcServer.jvmWithProtocol(UserProtocol(), http = InMemoryHttpServer).start()
        val protocol = JvmProtocolFactory.create<UserProtocol>(http = InMemoryHttpClient)
        val response = protocol.createLobby(PlayerId(123), "alo")
        assertEquals(CreateLobbyResponse(126), response)
        val response2 = protocol.killSomeone(111, PlayerId(5), Unit)
        assertEquals(116.toUInt(), response2)
        protocol.someShit(1, 2)
        protocol.someShit(1, 2)
        val mapForEntry = mapOf(1 to 1)
        protocol.moreTypes(
            listOf(),
            listOf(),
            1 to 2,
            Triple(Unit, PlayerId(1), ""),
            mapForEntry.entries.first()
        )
        val result = protocol.test(1 to 2)
        assertEquals(Triple(1, 2, "3") to 4.0, result)
        protocol.nullable(null, listOf(null))
        println("fioi")
        println("fioi")
        protocol.someShit(1, 2)
        val result2 = protocol.flowTest(2)
        assertFlowEquals(flowOf(listOf(PlayerId(2))), result2)

        protocol.genericTest("")
    }

    @Test
    fun testManual() = serverTest {
        RpcServer.jvmWithProtocol(SimpleProtocol(), http = InMemoryHttpServer).start()
        val protocol = JvmProtocolFactory.create<SimpleProtocol>(http = InMemoryHttpClient)
        val response = protocol.bar(2)
        assertEquals(3, response)
        val response2 = protocol.foo(4)
        assertFlowEquals(flowOf(5, 2, 3), response2)
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun <T> assertFlowEquals(flow1: Flow<T>, flow2: Flow<T>) {
        GlobalScope.launch {
            assertContentEquals(flow1.toList(), flow2.toList())
        }
    }

}