
import com.caesarealabs.rpc4k.runtime.user.Api
import java.util.Locale


@Api(true)
abstract class NonSerializableArgument {
    companion object;
    open suspend fun foo(thing: Locale) {

    }

}

