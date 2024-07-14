package com.caesarealabs.rpc4k.testapp

import com.caesarealabs.rpc4k.generated.BasicApiEventInvoker
import com.caesarealabs.rpc4k.runtime.user.Api
import com.caesarealabs.rpc4k.runtime.user.Dispatch
import com.caesarealabs.rpc4k.runtime.user.EventTarget
import com.caesarealabs.rpc4k.runtime.user.RpcEvent
import kotlinx.serialization.Serializable

@Api(true)
open class BasicApi(val invoker: BasicApiEventInvoker) {
    companion object;
    private val dogs = mutableListOf<Dog>()
    fun getDogs(num: Int, type: String): List<Dog> {
        return dogs.filter { it.type == type }.take(num)
    }

    suspend fun putDog(dog: Dog) {
        dogs.add(dog)
        invoker.invokeDogEvent(2, dog.name)
    }

    @RpcEvent
    fun dogEvent(@Dispatch dispatchParam: Int, @EventTarget target: String, clientParam: Boolean): Int {
        return if (clientParam) dispatchParam else dispatchParam + 2
    }
}

@Serializable
data class Dog(val name: String, val type: String, val age: Int)
