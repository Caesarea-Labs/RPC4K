
import com.caesarealabs.rpc4k.runtime.user.Api


@Api(true)
abstract class NoCompanionObject {
    open suspend fun foo(): List<Int> {
        error("Asdf")
    }
}

