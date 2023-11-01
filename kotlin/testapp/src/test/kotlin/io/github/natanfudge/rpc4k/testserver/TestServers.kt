@file:Suppress("TestFunctionName")

package io.github.natanfudge.rpc4k.testserver

import com.example.AllEncompassingService
import io.github.natanfudge.rpc4k.generated.server
import io.github.natanfudge.rpc4k.runtime.api.RpcServerSetup
import io.github.natanfudge.rpc4k.runtime.api.createServer
import io.github.natanfudge.rpc4k.test.BasicApi
import kotlin.test.Test

class TestServers {
    @Test
    fun myApi() {
        RpcServerSetup.managedKtor(BasicApi(), BasicApi.server()).createServer().start(wait = true)
    }

    @Test
    fun allEncompassingService() {
        RpcServerSetup.managedKtor(AllEncompassingService(), AllEncompassingService.server()).createServer().start(wait = true)
    }
}