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
