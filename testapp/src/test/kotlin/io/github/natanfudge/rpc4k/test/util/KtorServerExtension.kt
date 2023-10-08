package io.github.natanfudge.rpc4k.test.util

import io.github.natanfudge.rpc4k.runtime.api.Rpc
import io.github.natanfudge.rpc4k.runtime.api.components.KtorSingleRouteRpcServer
import io.github.natanfudge.rpc4k.runtime.api.old.utils.GeneratedServerHandler
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.utils.io.core.*
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.ExtensionContext
import java.net.ServerSocket
import java.net.SocketException

class KtorServerExtension(private val handler: (KtorSingleRouteRpcServer) -> GeneratedServerHandler) : Extension, BeforeAllCallback,
    AfterAllCallback {
    val port = getAvailablePort(8080, 8200)
    private val server = embeddedServer(CIO, port) {
        routing {
            post("/") {
                val server = handler(KtorSingleRouteRpcServer(call))
                val request = call.receiveChannel().readRemaining().readBytes()
                server.handle(request, Rpc.peekMethodName(request))
            }
        }
    }

    override fun beforeAll(context: ExtensionContext?) {
        server.start(wait = false)
    }

    override fun afterAll(context: ExtensionContext?) {
        server.stop()
    }
}

private fun getAvailablePort(startPort: Int, endPort: Int): Int {
    return try {
        ServerSocket(startPort).close()
        startPort
    } catch (e: SocketException) {
        if (startPort == endPort) error("Could not bind to any port in the range ${startPort}..$endPort!")
        getAvailablePort(startPort + 1, endPort)
    }
}