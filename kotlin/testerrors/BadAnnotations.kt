import com.caesarealabs.rpc4k.runtime.api.Api
import com.caesarealabs.rpc4k.runtime.api.Dispatch
import com.caesarealabs.rpc4k.runtime.api.EventTarget
import com.caesarealabs.rpc4k.runtime.api.RpcEvent

@Api(true)
open class InvalidPropertyType {
    companion object;
    @RpcEvent
    open suspend fun foo(@Dispatch @EventTarget dispatchTarget: String){
    }
    @RpcEvent
    open suspend fun foo2(@EventTarget target1: String,@EventTarget target2: String){
    }

    open suspend fun foo3(@Dispatch dispatch: String){
    }
    open suspend fun foo4(@EventTarget dispatch: String){
    }
}