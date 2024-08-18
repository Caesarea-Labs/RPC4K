package com.caesarealabs.rpc4k.test

import com.caesarealabs.rpc4k.generated.rpc4k
import com.caesarealabs.rpc4k.runtime.jvm.user.testing.junit
import com.caesarealabs.rpc4k.runtime.jvm.user.components.mongo.MongodbEventManager
import com.caesarealabs.rpc4k.runtime.jvm.user.testing.TestContainerMongoDb
import com.caesarealabs.rpc4k.testapp.MongoDbTestClass
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import strikt.api.expectThat
import strikt.assertions.isEqualTo

//TODO: Move to be a part of rpc4k itself


class MongoRpcTest {
    companion object {
        @JvmField @RegisterExtension
        val extension = MongoDbTestClass.rpc4k.junit (eventManager = MongodbEventManager(TestContainerMongoDb)) {
            MongoDbTestClass()
        }
    }

    @Test
    fun testKtorWithMongodbEvents() {
        runBlocking {
            var oneValues: Int? = null
            var twoValues: Int? = null
            GlobalScope.launch {
                extension.client.targetTest(1).collectLatest {
                    oneValues = it
                }
            }
            GlobalScope.launch {
                extension.client.targetTest(2).collectLatest {
                    twoValues = it
                }
            }
            delay(1000)

            extension.invoker.invokeTargetTest(3)
            delay(1000)
            expectThat(oneValues).isEqualTo(null)
            expectThat(twoValues).isEqualTo(null)
            extension.invoker.invokeTargetTest(2)
            delay(1000)
            expectThat(oneValues).isEqualTo(null)
            expectThat(twoValues).isEqualTo(2)
            extension.invoker.invokeTargetTest(1)
            delay(1000)
            expectThat(oneValues).isEqualTo(1)
            expectThat(twoValues).isEqualTo(2)
        }
    }
    @Test
    fun testKtorWithMongodbEventsWithoutTarget() {
        runBlocking {
            var oneValues: Int? = null
            var twoValues: Int? = null
            GlobalScope.launch {
                extension.client.noTargetTest(1).collectLatest {
                    oneValues = it
                }
            }
            GlobalScope.launch {
                extension.client.noTargetTest(2).collectLatest {
                    twoValues = it
                }
            }
            delay(5000)

            extension.invoker.invokeNoTargetTest()
            delay(1000)
            expectThat(oneValues).isEqualTo(1)
            expectThat(twoValues).isEqualTo(2)
            extension.invoker.invokeNoTargetTest()
            delay(1000)
            expectThat(oneValues).isEqualTo(1)
            expectThat(twoValues).isEqualTo(2)
            extension.invoker.invokeNoTargetTest()
            delay(1000)
            expectThat(oneValues).isEqualTo(1)
            expectThat(twoValues).isEqualTo(2)
        }
    }
}