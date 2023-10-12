package io.github.natanfudge.rpc4k.testapp.errorcases

import io.github.natanfudge.rpc4k.runtime.api.ApiClient


@ApiClient
abstract class NonSuspendingClientMethod {
    companion object;
    open fun foo(): List<Int> {
        error("Asdf")
    }
}

