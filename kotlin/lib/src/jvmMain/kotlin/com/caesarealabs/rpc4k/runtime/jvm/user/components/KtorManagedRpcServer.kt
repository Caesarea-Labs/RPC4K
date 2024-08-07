package com.caesarealabs.rpc4k.runtime.jvm.user.components

import com.caesarealabs.logging.LoggingFactory
import com.caesarealabs.logging.PrintLoggingFactory
import com.caesarealabs.rpc4k.runtime.api.*
import com.caesarealabs.rpc4k.runtime.user.Rpc4kIndex
import com.caesarealabs.rpc4k.runtime.user.components.JsonFormat
import com.caesarealabs.rpc4k.runtime.user.components.MemoryEventManager
import com.caesarealabs.rpc4k.runtime.user.startRpc
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set

/**
 * Configure and start an RPC server with one call from the [Rpc4kIndex]
 */
public fun <S, I> Rpc4kIndex<S, *, I>.startKtor(
    format: SerializationFormat = JsonFormat(),
    eventManager: EventManager = MemoryEventManager(),
    wait: Boolean = true,
    engine: ApplicationEngineFactory<*, *> = Netty,
    port: Int = PortPool.get(),
    ktorConfig: Application.() -> Unit = {},
    logging: LoggingFactory = PrintLoggingFactory,
    service: (I) -> S
): TypedServerConfig<S, I> = KtorManagedRpcServer(engine = engine, port = port, config = ktorConfig)
    .startRpc(this, format, eventManager, logging, wait, service)


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

    private var server: EmbeddedServer<*, *>? = null

    override fun start(config: ServerConfig, wait: Boolean) {
        server = embeddedServer(engine, port = port) {

            install(WebSockets)
            config()
            routing {
                post("/") {
                    Rpc4kKtor.routeCalls(call, config)
                }

                webSocket("/events") {
                    val connection = EventConnection(UUID.randomUUID().toString())
                    connections[connection] = this
                    try {
                        for (frame in incoming) {
                            config.acceptEventSubscription(frame.readBytes(), connection)
                        }
                    } finally {
                        config.config.logging.wrapCall("Ending Connections") {
                            logInfo { "Removing connection ${connection.id}" }
                        }
                        config.config.eventManager.dropClient(connection)
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