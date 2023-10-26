package io.github.natanfudge.rpc4k.testserver

import com.example.UserProtocol
import io.github.natanfudge.rpc4k.generated.UserProtocolServerImpl
import io.github.natanfudge.rpc4k.generated.server
import io.github.natanfudge.rpc4k.runtime.api.RpcServerSetup
import io.github.natanfudge.rpc4k.runtime.api.components.JsonFormat
import io.github.natanfudge.rpc4k.runtime.api.components.KtorManagedRpcServer
import io.github.natanfudge.rpc4k.runtime.api.createServer
import io.github.natanfudge.rpc4k.test.BasicApi
import io.ktor.server.netty.*
import kotlin.test.Test

class TestServers {
    @Test
    fun myApi() {
        RpcServerSetup.managedKtor(BasicApi(), BasicApi.server()).createServer().start(wait = true)
    }

    @Test
    fun userProtocol() {
        RpcServerSetup.managedKtor(UserProtocol(), UserProtocol.server()).createServer().start(wait = true)
    }
}