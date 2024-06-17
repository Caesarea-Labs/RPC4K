package com.caesarealabs.rpc4k.testapp

import com.caesarealabs.rpc4k.runtime.user.Api
import kotlinx.serialization.Serializable

@Api(true)
open class BasicApi {
    companion object;
    private val dogs = mutableListOf<Dog>()
    open suspend fun getDogs(num: Int, type: String): List<Dog> {
        return dogs.filter { it.type == type }.take(num)
    }

    open suspend fun putDog(dog: Dog) {
        dogs.add(dog)
    }
}
@Serializable
data class Dog(val name: String, val type: String, val age: Int)
