package com.caesarealabs.rpc4k.runtime.api.testing

import com.caesarealabs.rpc4k.runtime.api.*
import com.caesarealabs.rpc4k.runtime.api.components.JsonFormat
import com.caesarealabs.rpc4k.runtime.api.components.KtorManagedRpcServer
import com.caesarealabs.rpc4k.runtime.api.components.OkHttpRpcClient
import com.caesarealabs.rpc4k.runtime.implementation.PortPool
import okhttp3.OkHttpClient
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.ExtensionContext

//val x: Lazy

public fun <S, C, I> Rpc4kIndex<S, C, I>.junit(
    port: Int = PortPool.get(),
    format: SerializationFormat = JsonFormat(),
    server: RpcServerEngine.MultiCall = KtorManagedRpcServer(port = port),
    client: RpcClientFactory = RpcClientFactory.OkHttp(),
    serverFactory: (I) -> S,
): ClientServerExtension<S, C, I> {
    val url = "http://localhost:${port}"
    val websocketUrl = "$url/events"
//    val serverSetup = RpcServerSetup(serverHandler, rpc, server, format)
    val clientSetup = client.build(url, websocketUrl)
    val config = createHandlerConfig(format,server,serverFactory)
    val serverConfig = ServerConfig(router, config)
    val suite = Rpc4kSuite(config.handler, createNetworkClient(clientSetup, format), /*createMemoryClient(config.handler),*/ config.invoker)
    val engine = server.create(serverConfig)
    return ClientServerExtension(engine, suite)
}

//public fun <API, Invoker, C> rpcExtension(
//    serverHandler: (Invoker) -> API,
//    port: Int = PortPool.get(),
//    rpc: Rpc4kIndex<API, C, Invoker>,
////    generatedClass: GeneratedServerHelper<API, Invoker>,
////    generatedClient: GeneratedClientImplFactory<C>,
//    format: SerializationFormat = JsonFormat(),
//    server: RpcServerEngine.MultiCall = KtorManagedRpcServer(port = port),
//    client: RpcClientFactory<API> = RpcClientFactory.OkHttp(),
//): ClientServerExtension<API, Invoker, C> {
//    val url = "http://localhost:${port}"
//    val websocketUrl = "$url/events"
//    val serverSetup = RpcServerSetup(serverHandler, rpc, server, format)
//    val clientSetup = client.build(url, websocketUrl)
//    val invoker = rpc.createInvoker(serverSetup)
//    val handler = serverHandler(invoker)
//    val suite = Rpc4kSuite(handler, rpc.createNetworkClient(clientSetup, format), rpc.createMemoryClient(handler), invoker)
//    return ClientServerExtension(serverSetup, suite)
//}

//public inline fun <reified API, I, C> rpcExtension(
//    noinline serverHandler: (I) ->  API,
//    port: Int = PortPool.get(),
//    format: SerializationFormat = JsonFormat(),
//    server: RpcServerEngine.MultiCall = KtorManagedRpcServer(port = port),
//    client: RpcClientFactory<API> = RpcClientFactory.OkHttp()
//): ClientServerExtension<API, I, C> {
//    return rpcExtension(serverHandler, port, generatedServer(), apiClientFactory<API, C>(), format, server, client)
//}

public class Rpc4kSuite<Server, Client, Invoker>(
    public val server: Server,
    public val networkClient: Client,
//    public val memoryClient: Client,
    public val invoker: Invoker
)


//internal fun <Client, Invoker, Server> Rpc4kIndex<Server, Client, Invoker>.createFull(
//    server: Server,
//    serverSetup: HandlerConfig<Server>,
//    clientSetup: RpcClient
//): Rpc4kSuite<Server, Client, Invoker> {
//    val invoker = createInvoker(serverSetup)
//    return Rpc4kSuite(server, createNetworkClient(clientSetup,serverSetup.format), createMemoryClient(server), )
//}

//@Suppress("UNCHECKED_CAST")
//@PublishedApi
//internal inline fun <reified API, C> apiClientFactory(): GeneratedClientImplFactory<C> {
//    return Class.forName(GeneratedCodeUtils.Package + "." + API::class.simpleName + GeneratedCodeUtils.ClientSuffix)
//        .kotlin.companionObjectInstance as GeneratedClientImplFactory<C>
//}
//
//@Suppress("UNCHECKED_CAST")
//@PublishedApi
//internal inline fun <reified API, Invoker> generatedServer(): GeneratedServerHelper<API, Invoker> {
//    return Class.forName(GeneratedCodeUtils.Package + "." + API::class.simpleName + GeneratedCodeUtils.ServerSuffix)
//        .constructors[0].newInstance() as GeneratedServerHelper<API, Invoker>
//}


public class ClientServerExtension<S, C, I> internal constructor(
    private val engine: RpcServerEngine.MultiCall.Instance,
//    serverSetup: RpcServerEngine.MultiCall,
    suite: Rpc4kSuite<S, C, I>
) : Extension, BeforeAllCallback,
    AfterAllCallback {
    public val server: S = suite.server
    public val client: C = suite.networkClient

    //    public val server: S =  suite.server
    override fun beforeAll(context: ExtensionContext) {
        engine.start(wait = false)
    }

    override fun afterAll(context: ExtensionContext) {
        engine.stop()
    }
}


//TODO: this can probably be simplified
/**
 * Something that creates a [RpcClient], generally there's one [RpcClientFactory] per [RpcClient]
 * This interface is useful because it's often easier to specify a [RpcClient] than an instance of a [ServerExtension] because [ServerExtension]
 * often have many parameters.
 */
public interface RpcClientFactory {
    public fun build(url: String, websocketUrl: String): RpcClient

    public class OkHttp(private val client: OkHttpClient = OkHttpClient()) : RpcClientFactory {
        override fun build(url: String, websocketUrl: String): RpcClient = OkHttpRpcClient(url, websocketUrl, client)
    }
}
