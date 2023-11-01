@file:Suppress("NON_FINAL_MEMBER_IN_FINAL_CLASS", "UNUSED_PARAMETER", "EmptyMethod", "RedundantSuspendModifier")

package com.example

import io.github.natanfudge.rpc4k.runtime.api.Api

@Api(true)
abstract class TestValidClass1 {
    companion object;
    open suspend fun foo(thing: Int) {

    }

    abstract suspend fun bar(thing: Int)
}

@Api(true)
interface TestValidClass2 {
    companion object;
    suspend fun foo(thing: Int) {

    }

    suspend fun bar(thing: Int) {}
}

@Api
class TestValidClass3 {
    companion object;
    open fun foo(thing: Int) {

    }

    suspend fun bar(thing: Int) {}
}

@Api
open class TestValidClass4 {
    companion object;
    fun foo(thing: Int) {

    }

    suspend fun bar(thing: Int) {}
}
