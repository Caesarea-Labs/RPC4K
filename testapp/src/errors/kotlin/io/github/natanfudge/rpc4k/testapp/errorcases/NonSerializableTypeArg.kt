package io.github.natanfudge.rpc4k.testapp.errorcases

import io.github.natanfudge.rpc4k.runtime.api.ApiClient

@ApiClient
abstract class NonSerializableTypeArg {
    companion object;
    open suspend fun foo(): List<*> {
        error("Asdf")
    }

}

