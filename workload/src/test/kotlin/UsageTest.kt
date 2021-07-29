import com.example.CreateLobbyResponse
import com.example.PlayerId
import com.example.UserProtocol
import io.github.natanfudge.rpc4k.RpcClient
import io.github.natanfudge.rpc4k.RpcServer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class UsageTest {
    @Test
    fun testUsage() {
        val thing = Json.encodeToString(String.serializer(), "asdf")

        RpcServer.jvmStartWithProtocol(UserProtocol())
        val protocol = RpcClient.jvmWithProtocol<UserProtocol>()
        val response = protocol.createLobby(PlayerId(123), "alo")
        assertEquals(CreateLobbyResponse(126), response)
        val response2 = protocol.killSomeone(111, PlayerId(5), Unit)
        assertEquals(116.toUInt(), response2)
        protocol.someShit(1)
        protocol.someShit(1)
        val mapForEntry = mapOf(1 to 1)
        protocol.moreTypes(listOf(), listOf(), 1 to 2, Triple(Unit, PlayerId(1), ""), mapForEntry.entries.first())
        val result = protocol.test(1 to 2)
        assertEquals(Triple(1, 2, "3") to 4.0, result)
    }
}