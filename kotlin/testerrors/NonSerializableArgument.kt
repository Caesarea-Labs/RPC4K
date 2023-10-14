
import io.github.natanfudge.rpc4k.runtime.api.Api
import java.util.Locale


@Api(true)
abstract class NonSerializableArgument {
    companion object;
    open suspend fun foo(thing: Locale) {

    }

}

