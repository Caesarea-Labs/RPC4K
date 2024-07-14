package com.caesarealabs.rpc4k.test

import com.caesarealabs.rpc4k.generated.rpc4k
import com.caesarealabs.rpc4k.runtime.api.components.MemoryMulticallServer
import com.caesarealabs.rpc4k.runtime.api.components.MemoryRpcClient
import com.caesarealabs.rpc4k.runtime.jvm.user.testing.junit
import com.caesarealabs.rpc4k.testapp.BasicApi
import com.caesarealabs.rpc4k.testapp.Dog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.extension.RegisterExtension
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import kotlin.test.Test

class TestMemoryCommunication {
    companion object {
        @JvmField
        @RegisterExtension
        val extension = BasicApi.rpc4k.junit(server = { MemoryMulticallServer(it) }, client = {
            MemoryRpcClient(it)
        }) { BasicApi(it) }
    }


    @Test
    fun testManual(): Unit = runBlocking {
        var receivedValue: Int? = null

        val dog = Dog(name = "Natasha", type = "Shober", age = 12)
        GlobalScope.launch {
            extension.client.dogEvent(target = "Natasha", clientParam = false).collectLatest {
                receivedValue = it
            }
        }
        delay(200)

        extension.client.putDog(dog)
        val dogs = extension.client.getDogs(2, "Shober")
        expectThat(dogs).contains(dog)

        delay(400)
        expectThat(receivedValue).isEqualTo(4)
    }

}