@file:Suppress("NON_FINAL_MEMBER_IN_FINAL_CLASS")

package com.example

import io.github.natanfudge.rpc4k.runtime.api.ApiClient
import io.github.natanfudge.rpc4k.runtime.api.ApiServer

@ApiClient
abstract class TestValidClass1 {
    companion object;
    open suspend fun foo(thing: Int) {

    }

    abstract suspend fun bar(thing: Int)
}

@ApiClient
interface TestValidClass2 {
    companion object;
    suspend fun foo(thing: Int) {

    }

    suspend fun bar(thing: Int) {}
}

@ApiServer
class TestValidClass3 {
    companion object;
    open fun foo(thing: Int) {

    }

    suspend fun bar(thing: Int) {}
}

@ApiServer
open class TestValidClass4 {
    companion object;
    fun foo(thing: Int) {

    }

    suspend fun bar(thing: Int) {}
}
