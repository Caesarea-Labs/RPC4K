package testing

import com.example.*
import io.github.natanfudge.rpc4k.RpcClient
import io.github.natanfudge.rpc4k.RpcServer
import org.junit.Test
import kotlin.test.assertEquals

class UsageTest {
    @Test
    fun testUsage() {
        RpcServer.jvmStartWithProtocol(UserProtocol())
        val protocol = RpcClient.jvmWithProtocol<UserProtocol>()
        val response = protocol.createLobby(PlayerId(123), "alo")
        assertEquals(CreateLobbyResponse(126), response)
        val response2 = protocol.killSomeone(111, PlayerId(5), Unit)
        assertEquals(116.toUInt(), response2)
        protocol.someShit(1, 2)
        protocol.someShit(1, 2)
        val mapForEntry = mapOf(1 to 1)
        protocol.moreTypes(listOf(), listOf(), 1 to 2, Triple(Unit, PlayerId(1), ""), mapForEntry.entries.first())
        val result = protocol.test(1 to 2)
        assertEquals(Triple(1, 2, "3") to 4.0, result)
        protocol.nullable(null, listOf(null))
        println("fioi")
        println("fioi")
        protocol.someShit(1,2)
    }

}