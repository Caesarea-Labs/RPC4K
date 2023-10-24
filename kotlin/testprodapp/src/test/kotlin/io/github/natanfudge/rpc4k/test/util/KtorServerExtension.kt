package io.github.natanfudge.rpc4k.test.util

import io.github.natanfudge.rpc4k.runtime.api.GeneratedServerHandler
import io.github.natanfudge.rpc4k.runtime.api.components.KtorSingleRouteRpcServer
import io.github.natanfudge.rpc4k.runtime.api.components.ManagedKtorRpcServer
import io.ktor.server.netty.*
import org.junit.jupiter.api.extension.ExtensionContext

@JvmInline
value class Port(val value: Int)

class KtorServerExtension(private val handler: (KtorSingleRouteRpcServer) -> GeneratedServerHandler) : ServerExtension {
    override val port: Int = PortPool.get()
    private val server by lazy {
        ManagedKtorRpcServer(Netty, port, handler)
    }

    override fun beforeAll(context: ExtensionContext?) {
        server.start(wait = false)
    }

    override fun afterAll(context: ExtensionContext?) {
        server.stop()
    }
}



object PortPool {
    private var current = 8080

    @Synchronized
    fun get(): Int {
        if (current == 8200) error("Too many ports are being requested")
        return current++
    }
}
