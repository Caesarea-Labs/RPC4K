
import io.github.natanfudge.rpc4k.runtime.api.Api


@Api(true)
abstract class NoCompanionObject {
    open suspend fun foo(): List<Int> {
        error("Asdf")
    }
}

