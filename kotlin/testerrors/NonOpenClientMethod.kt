
import com.caesarealabs.rpc4k.runtime.api.Api


@Api(true)
open class NonOpenClientMethod {
    companion object;
    suspend fun foo(): List<Int> {
        error("Asdf")
    }
}

