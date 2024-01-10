package com.caesarealabs.rpc4k.runtime.api.components

import com.caesarealabs.rpc4k.runtime.api.*
import com.caesarealabs.rpc4k.runtime.implementation.KtorEventManager
import com.caesarealabs.rpc4k.runtime.implementation.KtorWebsocketEventConnection
import com.caesarealabs.rpc4k.runtime.implementation.PortPool
import com.caesarealabs.rpc4k.runtime.implementation.Rpc4K
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*

//TODO: allow splitting rpc server definitions across multiple classes/files




// NiceToHave: use a custom implementation that setups multiple routes
/**
 * Single class that sets up the ktor server for you
 * It sets up a single route at / to respond to rpc calls
 */
public class KtorManagedRpcServer(
    private val engine: ApplicationEngineFactory<*, *> = Netty,public val port: Int = PortPool.get(), private val config: Application.() -> Unit = {}
) : RpcServerEngine.MultiCall {

    private val singleRoute = KtorSingleRouteRpcServer()
    override val eventManager: EventManager<KtorWebsocketEventConnection> = KtorEventManager()

    private fun <RpcDef> Application.configImpl(setup: RpcSetupOf<RpcDef>) {
        install(CallLogging)
        install(WebSockets)

        config()
        routing {
            post("/") {
                setup.withEngine(engine = singleRoute).handleRequests(call, call)
            }

            webSocket("/events") {
                val connection = KtorWebsocketEventConnection(this)
                println("Adding connection ${connection.id}")
                try {
                    for (frame in incoming) {
                        frame as? Frame.Text ?: error("Unexpected non-text frame")
                        setup.acceptEventSubscription(frame.readBytes(), connection)
                    }
                } finally {
                    Rpc4K.Logger.info("Removing connection ${connection.id}")
                    eventManager.dropClient(connection)
                }
            }
        }
    }

    override fun <RpcDef> create(setup: RpcSetupOf<RpcDef>): RpcServerEngine.MultiCall.Instance = object : RpcServerEngine.MultiCall.Instance {
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


//private fun
//
//@Serializable
//data class FullClientMessage(
//    /**
//     * This will only be 'sendmessage' in AWS so everything routes to the same function
//     */
//    val action: String,
//    val message: String
//)
//
//@Serializable
//@SerialName("subscribe")
//data class SubscribeMessage<out T>(
//    val listenerId: String,
////    val event: String,
//    val params: T
//)