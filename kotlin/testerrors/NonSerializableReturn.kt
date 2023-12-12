
import com.caesarealabs.rpc4k.runtime.api.Api
import java.util.Locale


@Api(true)
abstract class NonSerializableReturn {
    companion object;
    open suspend fun foo(): Locale {
        error("Asdf")
    }

}

