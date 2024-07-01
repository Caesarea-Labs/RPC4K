package com.caesarealabs.rpc4k.testapp.test

import com.caesarealabs.rpc4k.generated.rpc4k
import com.caesarealabs.rpc4k.runtime.jvm.user.testing.junit
import com.caesarealabs.rpc4k.testapp.BasicApi
import com.caesarealabs.rpc4k.testapp.Dog
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.extension.RegisterExtension
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.test.Test


class KtorServerTest {
    companion object {
        @JvmField
        @RegisterExtension
        val extension = BasicApi.rpc4k.junit { BasicApi() }
    }


    @Test
    fun `Basic RPCs work`(): Unit = runBlocking {
        val client = extension.client
        val dog = Dog("asdf", "shiba", 2)
        client.putDog(dog)
        val dogs = client.getDogs(2, "shiba")
        expectThat(dogs).isEqualTo(listOf(dog))
    }
}


