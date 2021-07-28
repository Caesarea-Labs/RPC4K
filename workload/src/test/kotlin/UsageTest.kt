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
        val response2 = protocol.killSomeone(111,PlayerId(5),Unit)
        assertEquals(116.toUInt(),response2)
        protocol.someShit()
        protocol.someShit()
    }
}