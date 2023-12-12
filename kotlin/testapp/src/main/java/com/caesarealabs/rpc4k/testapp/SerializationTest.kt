package com.caesarealabs.rpc4k.testapp

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

@Serializable
sealed interface Foo {
    @Serializable
    data class Bar(val x: Int) : Foo
}

//@Serializable
//enum class Enum {
//    X,
//    Y,
//    Z
//}
//
fun main() {
    val map = """{"1": 2.0}"""
    val res = Json.decodeFromString(MapSerializer(Int.serializer(),Double.serializer()), map)
//    val
//    val res = Json.encodeToString(Foo.serializer(), Foo.Bar(2))
//    println(res)
}