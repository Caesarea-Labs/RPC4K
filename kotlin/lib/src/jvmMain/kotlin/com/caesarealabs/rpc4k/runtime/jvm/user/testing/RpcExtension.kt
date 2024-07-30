package com.caesarealabs.rpc4k.runtime.jvm.user.testing

import com.caesarealabs.logging.Logging
import com.caesarealabs.logging.LoggingFactory
import com.caesarealabs.logging.PrintLogging
import com.caesarealabs.logging.PrintLoggingFactory
import com.caesarealabs.rpc4k.runtime.api.*
import com.caesarealabs.rpc4k.runtime.user.components.JsonFormat
import com.caesarealabs.rpc4k.runtime.user.components.MemoryEventManager
import com.caesarealabs.rpc4k.runtime.implementation.createHandlerConfig
import com.caesarealabs.rpc4k.runtime.jvm.api.KtorManagedRpcServer
import com.caesarealabs.rpc4k.runtime.jvm.api.OkHttpRpcClient
import com.caesarealabs.rpc4k.runtime.user.Rpc4kIndex
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * Call in a companion object of a JUnit test class like so:
 * ```
 * MyTestClass {
 *      companion object {
 *         @JvmField
 *         @RegisterExtension
 *         val rpc = MyService.rpc4k.junit { MyService(it) }
 *      }
 * }
 * ```
 *
 * And make sure `JUnitPlatform` is activated:
 * ```
 * tasks.withType<Test> {
 *     useJUnitPlatform()
 * }
 * ```
 *
 * The returned object holds an instance of the running server - [ClientServerExtension.server],
 * and an instance of an RPC client [C] that may be used to access the RPC server [I] via network calls - [ClientServerExtension.client].
 *
 * As long as `@RegisterExtension` is properly annotated, the specified [server] will start in the specified [port] when tests start,
 * and will shut down when tests end. The server will use the [eventManager] to manage events.
 * The specified [client] will be used to access that server via network calls that are in the specified [format].
 *
 * @param service Your main `@Api` class should be constructed here. An event invoker will be passed to the lambda so that it may be injected
 * into the `@Api` class constructor, and used to invoke events.
 *
 * The [server] and [client] parameters should used the passed int as the port. It will reflect the [port] that is passed as a parameter.
 *
 *
 *
 */
public fun <S, C, I> Rpc4kIndex<S, C, I>.junit(
    port: Int = PortPool.get(),
    format: SerializationFormat = JsonFormat(),
    server: (port: Int) -> DedicatedServer = { KtorManagedRpcServer(port = it) },
    client: (port: Int, url: String, websocketUrl: String) -> RpcClient = { _, url, ws -> OkHttpRpcClient(url, ws) },
    eventManager: EventManager = MemoryEventManager(),
    logging: LoggingFactory = PrintLoggingFactory,
    service: (I) -> S,
): ClientServerExtension<S, C, I> {
    val url = "http://localhost:${port}"
    val websocketUrl = "ws://localhost:${port}/events"
    val serverInstance = server(port)
    val clientSetup = client(port, url, websocketUrl)
    val config = createHandlerConfig(format, eventManager, serverInstance, logging,service)
    val serverConfig = ServerConfig(router, config)
    return ClientServerExtension(
        serverInstance,
        serverConfig,
        port,
        createNetworkClient(clientSetup, format),
        config.handler,
        config.invoker
    )
}


public class ClientServerExtension<S, C, I> internal constructor(
    private val host: DedicatedServer,
    private val serverConfig: ServerConfig,
    /**
     * Exposed to make connecting to this specific server easier
     */
    public val port: Int,
    public val client: C,
    public val server: S,
    public val invoker: I,
) : Extension, BeforeAllCallback,
    AfterAllCallback {

    override fun beforeAll(context: ExtensionContext) {
        host.start(serverConfig, wait = false)
    }

    override fun afterAll(context: ExtensionContext) {
        host.stop()
        if (server is AutoCloseable) server.close()
    }
}


