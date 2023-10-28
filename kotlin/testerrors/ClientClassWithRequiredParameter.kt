
import io.github.natanfudge.rpc4k.runtime.api.Api


@Api(true)
open class ClientClassWithRequiredParameter(value: Int) {
    companion object;
    open suspend fun foo(): List<Int> {
        error("Asdf")
    }
}

