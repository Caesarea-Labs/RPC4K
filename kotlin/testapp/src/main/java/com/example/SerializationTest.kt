package com.example

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Foo(val x: Int, val y: String, val z: Boolean)

fun main() {
    val res = Json.encodeToString(Foo.serializer(),Foo(1, "hello", false))
}