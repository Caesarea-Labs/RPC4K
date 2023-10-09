package io.github.natanfudge.rpc4k.testapp.errorcases

import io.github.natanfudge.rpc4k.runtime.api.ApiClient

//TODO: add error tests for the following cases:
// 1. Non-serializable argument
// 2. Non-serializable return type
// 3. non-suspending client method
// 4. non-open client method
// 5. non-open client class

@ApiClient
abstract class NonSerializableTypeArg {
    companion object;
    open suspend fun foo(): List<*> {
        error("Asdf")
    }

}

