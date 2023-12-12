import com.caesarealabs.rpc4k.runtime.api.Api

@Api
class SealedTypeProperty {
    companion object;
    fun doStuff(t: SealedType) {

    }
}

@Serializable
sealed interface SealedType {
    @Serializable
    class GenericSubclass(val type: String) : GenericSealed
}