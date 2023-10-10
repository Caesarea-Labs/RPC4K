package io.github.natanfudge.rpc4k.testapp.errorcases

import io.github.natanfudge.rpc4k.runtime.api.ApiClient


@ApiClient
open class NonOpenClientMethod {
    companion object;
    suspend fun foo(): List<Int> {
        error("Asdf")
    }
}

