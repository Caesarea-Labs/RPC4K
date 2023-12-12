
import com.caesarealabs.rpc4k.runtime.api.Api


@Api(true)
open class NonContextualPair {
    companion object;
    open suspend fun foo(): Foo {
        error("Asdf")
    }
}

data class Foo(val x: Pair<Int,Int>)