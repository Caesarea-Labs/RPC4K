package com.caesarealabs.rpc4k.runtime.jvm.api

import com.caesarealabs.rpc4k.runtime.api.EventConnection
import com.caesarealabs.rpc4k.runtime.api.RpcServerEngine
import com.caesarealabs.rpc4k.runtime.api.ServerConfig
import com.caesarealabs.rpc4k.runtime.implementation.*
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.ApplicationEngineFactory
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set



// NiceToHave: use a custom implementation that setups multiple routes
/**
 * Single class that sets up the ktor server for you
 * It sets up a single route at `/` to respond to rpc calls
 */
public class KtorManagedRpcServer(
    private val engine: ApplicationEngineFactory<*, *> = Netty,
    public val port: Int = PortPool.get(),
    private val config: Application.() -> Unit = {}
) : RpcServerEngine.MultiCall {

    private val connections = ConcurrentHashMap<EventConnection, DefaultWebSocketSession>()

    private val singleRoute = KtorSingleRouteRpcServer()

    private fun Application.configImpl(config: ServerConfig) {
        install(CallLogging)
        install(WebSockets)

        config()
        routing {
            post("/") {
                singleRoute.routeRpcs(call, call, config)
            }

            webSocket("/events") {
                val connection = EventConnection(UUID.randomUUID().toString())
                connections[connection] = this
                println("Adding connection ${connection.id}")
                try {
                    for (frame in incoming) {
                        frame as? Frame.Text ?: error("Unexpected non-text frame")
                        config.acceptEventSubscription(frame.readBytes(), connection)
                    }
                } finally {
                    Rpc4kLogger.info("Removing connection ${connection.id}")
                    config.eventManager.dropClient(connection)
                    connections.remove(connection)
                }
            }
        }
    }

    override fun create(config: ServerConfig): RpcServerEngine.MultiCall.Instance =
        object : RpcServerEngine.MultiCall.Instance {
            private val server = embeddedServer(engine, port = port) {
                configImpl(config)
            }

            override fun stop() {
                server.stop()
            }

            override fun start(wait: Boolean) {
                server.start(wait)
            }
        }

    override suspend fun sendMessage(connection: EventConnection, bytes: ByteArray): Boolean {
        return connections[connection]?.send(Frame.Text(true, bytes)) != null
    }
}
