package io.github.natanfudge.rpc4k.test

import io.github.natanfudge.rpc4k.runtime.api.*
import io.github.natanfudge.rpc4k.runtime.api.components.JsonFormat
import io.github.natanfudge.rpc4k.runtime.api.components.KtorSingleRouteRpcServer
import io.github.natanfudge.rpc4k.runtime.api.components.OkHttpRpcClient
import io.github.natanfudge.rpc4k.runtime.api.utils.GeneratedCodeUtils
import io.github.natanfudge.rpc4k.runtime.impl.RpcServerException
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

fun main(): Unit = runBlocking {
    MyApiGeneratedClient(OkHttpRpcClient("http://localhost:8080"), JsonFormat()).getDogs(2, "Asdf")
}


private fun startServer() = embeddedServer(CIO, 8080) {
    routing {
        post("/") {
            val server = MyApiGeneratedServer(MyApi(), JsonFormat(), KtorSingleRouteRpcServer(call))
            val request = call.receiveChannel().readRemaining().readBytes()
            server.handle(request, Rpc.peekMethodName(request))
        }
    }
}

class MyApi {
    private val dogs = mutableListOf<Dog>()
    fun getDogs(num: Int, type: String): List<Dog> {
        return dogs.filter { it.type == type }.take(num)
    }

    fun putDog(dog: Dog) {
        dogs.add(dog)
    }
}


class MyApiGeneratedServer(private val api: MyApi, private val format: SerializationFormat, private val server: RpcServer) {
    suspend fun handle(request: ByteArray, method: String) {
        GeneratedCodeUtils.handle(server) {
            when (method) {
                "getDogs" -> GeneratedCodeUtils.respond(format, server, request, listOf(Int.serializer(), String.serializer()), ListSerializer(Dog.serializer())) {
                    api.getDogs(it[0] as Int, it[1] as String)
                }

                "putDog" -> GeneratedCodeUtils.respond(format, server, request, listOf(Dog.serializer()), Unit.serializer()) {
                    api.putDog(it[0] as Dog)
                }
            }
        }
    }
}



class MyApiGeneratedClient(private val client: RespondingRpcClient, private val format: SerializationFormat) {
    suspend fun getDogs(num: Int, type: String): Dog {
        return GeneratedCodeUtils.send(client, format, "getDogs", listOf(num, type), listOf(Int.serializer(), String.serializer()), Dog.serializer())
    }

    suspend fun putDog(dog: Dog) {
        GeneratedCodeUtils.send(client, format, "putDog", listOf(dog), listOf(Dog.serializer()))
    }
}


@Serializable
data class Dog(val name: String, val type: String, val age: Int)