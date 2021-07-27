import com.example.*
import io.github.natanfudge.rpc4k.RpcClient
import org.junit.Test
import kotlin.test.assertEquals

class UsageTest {
    @Test
    fun testUsage() {
        userServer()
        val protocol = RpcClient.jvmWithProtocol(UserProtocol::class)
        val response = protocol.createLobby(PlayerId(123), "alo")
        assertEquals(CreateLobbyResponse(8), response)
    }
}