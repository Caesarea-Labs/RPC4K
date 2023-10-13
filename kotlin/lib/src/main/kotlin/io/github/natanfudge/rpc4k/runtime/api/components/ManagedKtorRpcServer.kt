package io.github.natanfudge.rpc4k.runtime.api.components

import io.github.natanfudge.rpc4k.runtime.api.GeneratedServerHandler
import io.github.natanfudge.rpc4k.runtime.api.Rpc
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.utils.io.core.*
import org.slf4j.event.*

/**
 * Single class that sets up the ktor server for you
 * It sets up a single route at / to respond to rpc calls
 */
class ManagedKtorRpcServer(engine: ApplicationEngineFactory<*, *>, port: Int, private val handler: (KtorSingleRouteRpcServer) -> GeneratedServerHandler) {
    private val server = embeddedServer(engine, port = port) {
        install(CallLogging)
        routing {
            post("/") {
                val server = handler(KtorSingleRouteRpcServer(call))
                val request = call.receiveChannel().readRemaining().readBytes()
                server.handle(request, Rpc.peekMethodName(request))
            }
        }
    }

    fun start(wait: Boolean) {
        server.start(wait)
    }

    fun stop() {
        server.stop()
    }
}