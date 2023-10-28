import kotlinx.serialization.Serializable
import io.github.natanfudge.rpc4k.runtime.api.Api
import java.util.Locale


@Api(true)
open class InvalidPropertyType {
    companion object;
    open suspend fun foo(foo: Foo): List<Int> {
        error("Asdf")
    }
}
@Serializable
data class Foo(@Contextual val locale: Locale)