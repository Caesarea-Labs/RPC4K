package io.github.natanfudge.rpc4k.test

import io.github.natanfudge.rpc4k.runtime.api.ApiClient
import io.github.natanfudge.rpc4k.runtime.api.ApiServer
import io.github.natanfudge.rpc4k.test.util.rpcExtension
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.extension.RegisterExtension
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.test.Test


class KtorServerTest {
    companion object {
        @JvmField
        @RegisterExtension
        val extension = rpcExtension(BasicApi())
    }

    @Test
    fun `Basic RPCs work`(): Unit = runBlocking {
        val client = extension.api
        val dog = Dog("asdf", "shiba", 2)
        client.putDog(dog)
        val dogs = client.getDogs(2, "shiba")
        expectThat(dogs).isEqualTo(listOf(dog))
    }
}

@ApiClient
@ApiServer
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
