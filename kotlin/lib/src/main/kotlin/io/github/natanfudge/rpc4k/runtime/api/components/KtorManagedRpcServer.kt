package io.github.natanfudge.rpc4k.runtime.api.components

import io.github.natanfudge.rpc4k.runtime.api.RpcServerEngine
import io.github.natanfudge.rpc4k.runtime.api.RpcServerSetup
import io.github.natanfudge.rpc4k.runtime.api.handle
import io.github.natanfudge.rpc4k.runtime.api.withEngine
import io.github.natanfudge.rpc4k.runtime.implementation.PortPool
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.routing.*

/**
 * Single class that sets up the ktor server for you
 * It sets up a single route at / to respond to rpc calls
 */
class KtorManagedRpcServer(
    private val engine: ApplicationEngineFactory<*, *> = Netty, val port: Int = PortPool.get(), private val config: Application.() -> Unit = {}
) : RpcServerEngine.MultiCall {

    // NiceToHave: use a custom implementation that setups multiple routes
    private val singleRoute = KtorSingleRouteRpcServer()

    private fun Application.configImpl(setup: RpcServerSetup<*, *>) {
        install(CallLogging)
        config()
        routing {
            post("/") {
                setup.withEngine(engine = singleRoute).handle(call, call)
            }
        }
    }

    override fun create(setup: RpcServerSetup<*, *>) = object : RpcServerEngine.MultiCall.Instance {
        private val server = embeddedServer(engine, port = port) {
            configImpl(setup)
        }

        override fun stop() {
            server.stop()
        }

        override fun start(wait: Boolean) {
            server.start(wait)
        }
    }

}