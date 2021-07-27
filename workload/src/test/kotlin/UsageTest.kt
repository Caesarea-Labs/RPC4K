import com.example.CreateLobbyResponse
import com.example.GeneratedUserProtocolManual
import com.example.PlayerId
import com.example.userServer
import io.github.natanfudge.rpc4k.RpcClient
import org.junit.Test
import kotlin.test.assertEquals

class UsageTest {
    @Test
    fun testUsage() {
        userServer()
        val protocol = GeneratedUserProtocolManual(RpcClient())
        val response = protocol.createLobby(PlayerId(123), "alo")
        assertEquals(CreateLobbyResponse(8), response)
    }
}