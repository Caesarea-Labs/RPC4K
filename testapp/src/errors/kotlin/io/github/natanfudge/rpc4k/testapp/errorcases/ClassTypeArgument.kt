package io.github.natanfudge.rpc4k.testapp.errorcases

import io.github.natanfudge.rpc4k.runtime.api.ApiServer

@ApiServer
class ClassTypeArgument<T> {
    companion object;
    fun doStuff(t: T) {

    }
}