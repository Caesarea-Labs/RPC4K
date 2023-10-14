
import io.github.natanfudge.rpc4k.runtime.api.Api


@Api(true)
abstract class NonSuspendingClientMethod {
    companion object;
    open fun foo(): List<Int> {
        error("Asdf")
    }
}

