package io.github.natanfudge.rpc4k_test

import com.example.SimpleProtocol
import io.github.natanfudge.rpc4k.runtime.api.Port
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class RealServerSimpleTest {

    private val config = RealServerTestConfig.create(Port(8080), SimpleProtocol())
    private val client = config.client

    @Before
    fun before()  = config.before()

    @After
    fun after() = config.after()

    @Test
    fun testManual() = serverTest {
        val response = client.bar(2)
        assertEquals(3, response)
        val response2 = client.foo(4)
        assertFlowEquals(flowOf(5, 2, 3), response2)
//        response2.collect { println("Collecting: $it") }
    }

    @Test
    fun tdd() = serverTest {
         client.foo(4)
    }


    @OptIn(DelicateCoroutinesApi::class)
    fun <T> assertFlowEquals(flow1: Flow<T>, flow2: Flow<T>) {
        GlobalScope.launch {
            assertContentEquals(flow1.toList(), flow2.toList())
        }
    }

}