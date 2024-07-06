package com.caesarealabs.rpc4k.runtime.jvm.api

import com.caesarealabs.rpc4k.runtime.api.*
import com.caesarealabs.rpc4k.runtime.implementation.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.util.*
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
) : DedicatedServer {

    private val connections = ConcurrentHashMap<EventConnection, DefaultWebSocketSession>()

    private var server: ApplicationEngine? = null

    override fun start(config: ServerConfig, wait: Boolean) {
        server = embeddedServer(engine, port = port) {
            install(CallLogging)
            install(WebSockets)

            config()
            routing {
                post("/") {
                    Rpc4kKtor.routeCalls(call, config)
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
        server!!.start(wait)
    }

    override fun stop() {
        server?.stop()
    }

    override suspend fun send(connection: EventConnection, bytes: ByteArray): Boolean {
        if (server == null) throw IllegalStateException("Server not initialized")
        return connections[connection]?.send(Frame.Text(true, bytes)) != null
    }
}