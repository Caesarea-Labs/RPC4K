@file:Suppress("ExtractKtorModule")

package io.github.natanfudge.rpc4k.test

import io.github.natanfudge.rpc4k.generated.MyApiClientImpl
import io.github.natanfudge.rpc4k.generated.MyApiServerImpl
import io.github.natanfudge.rpc4k.runtime.api.*
import io.github.natanfudge.rpc4k.runtime.api.components.JsonFormat
import io.github.natanfudge.rpc4k.runtime.api.components.KtorSingleRouteRpcServer
import io.github.natanfudge.rpc4k.runtime.api.components.OkHttpRpcClient
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

fun main(): Unit = runBlocking {
    startServer()
    val client = MyApiClientImpl(OkHttpRpcClient("http://localhost:8080"), JsonFormat())
    client.putDog(Dog("asdf", "shiba", 2))
    val dogs = client.getDogs(2, "shiba")
    println(dogs)
    val x = 2
}


private fun startServer() = embeddedServer(CIO, 8080) {
    val api = MyApi()
    routing {
        post("/") {
            val server = MyApiServerImpl(api, JsonFormat(), KtorSingleRouteRpcServer(call))
            val request = call.receiveChannel().readRemaining().readBytes()
            server.handle(request, Rpc.peekMethodName(request))
        }
    }
}.start(wait = false)

//TODO:
// 1. split the @Api api to @ApiClient and @ApiServer (make sure to not do work twice when both are specified)
// 2. When @ApiClient is specified, the class and its methods must be open because the generated api class is supposed to implement it.
// 3. make generated class implement the original api interface / abstract class.

@Api
class MyApi {
    private val dogs = mutableListOf<Dog>()
    fun getDogs(num: Int, type: String): List<Dog> {
        return dogs.filter { it.type == type }.take(num)
    }

    fun putDog(dog: Dog) {
        dogs.add(dog)
    }
}

@Serializable
data class Dog(val name: String, val type: String, val age: Int)