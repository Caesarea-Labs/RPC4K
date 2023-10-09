package io.github.natanfudge.rpc4k_test

import com.example.SimpleProtocol
import io.github.natanfudge.rpc4k.generated.MyApiServerImpl
import io.github.natanfudge.rpc4k.runtime.api.components.JsonFormat
import io.github.natanfudge.rpc4k.test.MyApi
import io.github.natanfudge.rpc4k.test.util.KtorServerExtension
import io.github.natanfudge.rpc4k.test.util.rpcExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.test.Test

class RealServerSimpleTest {

    companion object {
        private val api = SimpleProtocol()

        @JvmField
        @RegisterExtension
        val extension = rpcExtension(api)
    }


    @Test
    fun testManual() = serverTest {
        val response = api.bar(2)
        assertEquals(3, response)
//        val response2 = client.foo(4)
//        assertFlowEquals(flowOf(5, 2, 3), response2)
//        response2.collect { println("Collecting: $it") }
    }

    @Test
    fun tdd() = serverTest {
        api.foo(4)
    }


//    @OptIn(DelicateCoroutinesApi::class)
//    fun <T> assertFlowEquals(flow1: Flow<T>, flow2: Flow<T>) {
//        GlobalScope.launch {
//            assertContentEquals(flow1.toList(), flow2.toList())
//        }
//    }

}