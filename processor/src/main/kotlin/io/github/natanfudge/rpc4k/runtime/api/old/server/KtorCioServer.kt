package io.github.natanfudge.rpc4k.runtime.api.old.server

import io.github.natanfudge.rpc4k.runtime.api.old.client.UnauthorizedException
import io.github.natanfudge.rpc4k.runtime.api.old.Logger
import io.github.natanfudge.rpc4k.runtime.api.old.Port
import io.github.natanfudge.rpc4k.runtime.implementation.old.Sse
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.request.receiveStream
import io.ktor.server.response.cacheControl
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondTextWriter
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.Writer
import kotlin.system.measureTimeMillis


private enum class NetworkDelay(val millis: Long) {
    None(0),
    Normal(70),
    BadInternet(200),
    Abnormal(1000)
}

private val networkDelayEmulation = NetworkDelay.None

class KtorCioServer(private val logger: Logger, port: Port) : RpcHttpServer {
    private lateinit var requestHandler: RequestHandler
    private lateinit var flowRequestHandler: FlowRequestHandler


    private val engine = embeddedServer(CIO, port = port.value) {
        config()
    }

    private fun Application.config() {
        logger.info { "Starting server" }
        //        install(CallLogging) {
        //            level = Level.INFO
        //        }
        routing {
            fun handleRoute(
                route: String,
                handler: suspend (path: String, body: ByteArray, call: ApplicationCall) -> Unit
            ) {
                val methodRouteParameter = "method"
                post("$route{$methodRouteParameter}") {
                    try {
                        call.receiveStream().use {
                            handler(call.parameters[methodRouteParameter]!!, it.readBytes(), call)
                        }
                    } catch (e: IllegalArgumentException) {
                        e.printStackTrace()
                        // The server uses require() for validating client side requests
                        call.respond(
                            HttpStatusCode.ExpectationFailed.copy(description = e.message ?: "Requirement failed")
                        )
                    } catch (e: UnauthorizedException) {
                        e.printStackTrace()
                        call.respond(HttpStatusCode.Unauthorized.copy(description = e.message ?: "Unauthorized"))
                    }
                }
            }
            handleRoute("/") { path, body, call ->
                delay(networkDelayEmulation.millis)
                val response = requestHandler(path, body)
                call.respondBytes(response)
            }
            handleRoute("/flow/") { path, body, call ->
                delay(networkDelayEmulation.millis)
                val response = flowRequestHandler(path, body)
                call.respondSse(response.map { Sse(it.toString(Charsets.UTF_8)) })
            }
        }
    }


    override fun start(requestHandler: RequestHandler, flowRequestHandler: FlowRequestHandler){
        this.requestHandler = requestHandler
        this.flowRequestHandler = flowRequestHandler
        engine.start(wait = false)


//        engine.application.attributes.


    }

    override fun stop() {
        val fast = true
        logger.info { "Stopping server" }
        val closeTime = measureTimeMillis {
            engine.stop(gracePeriodMillis = if (fast) 20 else 500, timeoutMillis = 5000)
        }
        logger.debug { "Stopped server in $closeTime ms" }
    }
}

private suspend fun ApplicationCall.respondSse(events: Flow<Sse>) {
    response.cacheControl(CacheControl.NoCache(null))
    respondTextWriter(contentType = ContentType.Text.EventStream) {
        events.collect {
            write(it)
        }
    }
}

private fun Writer.write(sse: Sse) {
    if (sse.id != null) {
        write("id: ${sse.id}\n")
    }
    if (sse.event != null) {
        write("event: ${sse.event}\n")
    }
    for (dataLine in sse.data.lines()) {
        write("data: $dataLine\n")
    }
    write("\n")
    flush()
}

