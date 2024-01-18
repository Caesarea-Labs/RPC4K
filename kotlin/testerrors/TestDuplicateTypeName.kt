package com.caesarealabs.rpc4k.testapp

import com.caesarealabs.rpc4k.runtime.api.Api
import kotlinx.serialization.Serializable


@Api
class TestDuplicateTypeName() {
    companion object;
    fun doSomething(foo: Foo3): Parent {
        return Parent.Foo3()
    }
}

@Serializable
sealed class Parent {
    @Serializable
    class Foo3 : Parent()
}




@Serializable
class Foo3