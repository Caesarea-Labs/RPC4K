package io.github.natanfudge.rpc4k.testapp.errorcases

import io.github.natanfudge.rpc4k.runtime.api.ApiClient
import java.util.Locale


@ApiClient
abstract class NonSerializableReturn {
    companion object;
    open suspend fun foo(): Locale {
        error("Asdf")
    }

}

