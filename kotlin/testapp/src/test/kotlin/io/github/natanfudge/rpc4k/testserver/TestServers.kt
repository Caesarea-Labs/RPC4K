package io.github.natanfudge.rpc4k.testserver

import io.github.natanfudge.rpc4k.generated.BasicApiServerImpl
import io.github.natanfudge.rpc4k.runtime.api.components.JsonFormat
import io.github.natanfudge.rpc4k.runtime.api.components.ManagedKtorRpcServer
import io.github.natanfudge.rpc4k.test.BasicApi
import io.ktor.server.netty.*
import kotlin.test.Test

class TestServers {
    @Test
    fun myApi() {
        val api = BasicApi()
        ManagedKtorRpcServer(Netty, port = 8080) {
            BasicApiServerImpl(api, JsonFormat(), it)
        }.start(wait = true)
    }
}