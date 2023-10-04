package io.github.natanfudge.rpc4k_test

import io.github.natanfudge.rpc4k.runtime.api.Port
import io.github.natanfudge.rpc4k.runtime.api.client.JvmProtocolFactory
import io.github.natanfudge.rpc4k.runtime.api.client.OkHttpRpcClient
import io.github.natanfudge.rpc4k.runtime.api.server.KtorCioServer
import io.github.natanfudge.rpc4k.runtime.api.server.RpcServer
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass

class RealServerTestConfig<T : Any>(port: Port, protocol: T, protocolClass: KClass<T>) {
    companion object {
        inline fun <reified T : Any> create(port: Port, protocol: T) = RealServerTestConfig(port, protocol, T::class)
    }

    private val server = RpcServer.jvmWithProtocol(protocol, KtorCioServer(TestServerLogger, port), protocolClass)
    val client = JvmProtocolFactory.create(OkHttpRpcClient(TestClientLogger, port), protocolClass)

    fun before() {
        runBlocking {
            server.start()
        }
    }

    fun after() {
        server.stop()
    }
}