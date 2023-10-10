package io.github.natanfudge.rpc4k.test.util

import io.github.natanfudge.rpc4k.runtime.api.GeneratedServerImpl
import io.github.natanfudge.rpc4k.runtime.api.Rpc
import io.github.natanfudge.rpc4k.runtime.api.components.KtorSingleRouteRpcServer
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.utils.io.core.*
import org.junit.jupiter.api.extension.ExtensionContext
import java.net.BindException
import java.net.ServerSocket
import java.net.SocketException

@JvmInline value class Port(val value: Int)

class KtorServerExtension(private val handler: (KtorSingleRouteRpcServer) -> GeneratedServerImpl) : ServerExtension {
    override val port: Int = PortPool.get()
    private val server by lazy {
        embeddedServer(CIO, port = port) {
            routing {
                post("/") {
                    val server = handler(KtorSingleRouteRpcServer(call))
                    val request = call.receiveChannel().readRemaining().readBytes()
                    server.handle(request, Rpc.peekMethodName(request))
                }
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

//private fun embeddedServerOnAvailablePort(startPort: Int, endPort: Int, config: Application.() -> Unit): Pair<ApplicationEngine, Int> {
//    return try {
//        val server = embeddedServer(CIO, port = startPort) { config() }
//        server.start(wait = false)
//        server to startPort
//    } catch (e: BindException) {
//        if (startPort == endPort) error("Could not bind to any port in the range ${startPort}..$endPort!")
//        embeddedServerOnAvailablePort(startPort + 1, endPort, config)
//    }
//}

object PortPool {
    private var current = 8080

    @Synchronized
    fun get(): Int {
        if (current == 8200) error("Too many ports are being requested")
        return current++
    }
}
