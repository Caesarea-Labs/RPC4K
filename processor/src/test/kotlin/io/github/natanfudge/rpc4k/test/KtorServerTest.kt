package io.github.natanfudge.rpc4k.test

import io.github.natanfudge.rpc4k.runtime.api.components.JsonFormat
import io.github.natanfudge.rpc4k.runtime.api.components.OkHttpRpcClient
import io.github.natanfudge.rpc4k.test.util.KtorServerExtension
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.junit.jupiter.api.extension.RegisterExtension
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.test.Test

class KtorServerTest {
    companion object {
        private val api = MyApi()

        @JvmField
        @RegisterExtension
        val ktor = KtorServerExtension { MyApiGeneratedServer(api, JsonFormat(), it) }
    }

    @Test
    fun `Basic RPCs work`(): Unit = runBlocking {
        val client = MyApiGeneratedClient(OkHttpRpcClient("http://localhost:${ktor.port}"), JsonFormat())
        val dog = Dog("asdf", "shiba", 2)
        client.putDog(dog)
        val dogs = client.getDogs(2, "shiba")
        expectThat(dogs).isEqualTo(listOf(dog))
    }
}

//TODO:
// 1. Define data structure for an API call.


//TODO:
// 2. Use KSP to convert an @Api class into an APIDefinition
// 3. Write  ApiDefinition -> Server handler gen
// 4. Write ApiDefinition -> Client gen
