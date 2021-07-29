import io.github.natanfudge.rpc4k.decodeSplitable
import io.github.natanfudge.rpc4k.encodeToSplitable
import kotlin.test.Test
import kotlin.test.assertContentEquals

class EncodingTest {
    @Test
    fun testSplitable() {
        val array = "hello there".toByteArray()
        val encoded = array.encodeToSplitable()
        val decoded = encoded.decodeSplitable()
        //TODO: bad
        assertContentEquals(array, decoded)
    }
}