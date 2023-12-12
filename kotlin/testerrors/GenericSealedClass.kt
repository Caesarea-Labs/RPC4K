import com.caesarealabs.rpc4k.runtime.api.Api
import kotlinx.serialization.Serializable

@Api
class GenericSealedClass {
    companion object;
    fun doStuff(t: GenericSealed<Int>) {

    }
}

@Serializable
sealed interface GenericSealed<out T1> {
    @Serializable
    class GenericSubclass<T> : GenericSealed<T>
}