package io.github.natanfudge.rpc4k.test.util

import io.github.natanfudge.rpc4k.runtime.api.RpcServerEngine
import io.github.natanfudge.rpc4k.runtime.api.RpcServerSetup
import io.github.natanfudge.rpc4k.runtime.api.createServer
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.ExtensionContext

@JvmInline
value class Port(val value: Int)

class MultiCallServerExtension(setup: RpcServerSetup<*,RpcServerEngine.MultiCall>) : Extension, BeforeAllCallback, AfterAllCallback {
    private val server = setup.createServer()
    override fun beforeAll(context: ExtensionContext?) {
        server.start(wait = false)
    }

    override fun afterAll(context: ExtensionContext?) {
        server.stop()
    }

}

//class KtorServerExtension(private val handler: (KtorSingleRouteRpcServer) -> GeneratedServerHelper) : ServerExtension {
//    override val port: Int = PortPool.get()
//    private val server by lazy {
//        KtorManagedRpcServer(Netty, port, handler)
//    }
//
//    override fun beforeAll(context: ExtensionContext?) {
//        server.start(wait = false)
//    }
//
//    override fun afterAll(context: ExtensionContext?) {
//        server.stop()
//    }
//}



