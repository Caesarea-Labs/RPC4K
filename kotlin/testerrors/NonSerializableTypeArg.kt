
import com.caesarealabs.rpc4k.runtime.api.Api

@Api(true)
abstract class NonSerializableTypeArg {
    companion object;
    open suspend fun foo(): List<*> {
        error("Asdf")
    }

}

