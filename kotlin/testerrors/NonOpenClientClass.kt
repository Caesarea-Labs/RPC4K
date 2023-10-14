
import io.github.natanfudge.rpc4k.runtime.api.Api


@Api(true)
class NonOpenClientClass {
    companion object;
    open suspend fun foo(): List<Int> {
        error("Asdf")
    }
}

