package io.github.natanfudge.rpc4k.testapp.errorcases

import io.github.natanfudge.rpc4k.runtime.api.ApiClient


@ApiClient
class NonOpenClientClass {
    companion object;
    open suspend fun foo(): List<Int> {
        error("Asdf")
    }
}

