package com.caesarealabs.rpc4k.runtime.api.testing

import com.caesarealabs.rpc4k.runtime.api.*
import com.caesarealabs.rpc4k.runtime.api.components.JsonFormat
import com.caesarealabs.rpc4k.runtime.api.components.KtorManagedRpcServer
import com.caesarealabs.rpc4k.runtime.api.components.OkHttpRpcClient
import com.caesarealabs.rpc4k.runtime.implementation.GeneratedCodeUtils
import com.caesarealabs.rpc4k.runtime.implementation.MultiCallServerExtension
import com.caesarealabs.rpc4k.runtime.implementation.PortPool
import okhttp3.OkHttpClient
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.ExtensionContext
import kotlin.reflect.full.companionObjectInstance

public fun <API, Invoker> rpcExtension(
    serverHandler: (Invoker) ->  API,
    port: Int = PortPool.get(),
    generatedClass: GeneratedServerHelper<API, Invoker>,
    generatedClient: GeneratedClientImplFactory<API>,
    format: SerializationFormat = JsonFormat(),
    server: RpcServerEngine.MultiCall = KtorManagedRpcServer(port = port),
    client: RpcClientFactory<API> = RpcClientFactory.OkHttp(),
): ClientServerExtension<API, Invoker> {
    val url = "http://localhost:${port}"
    val websocketUrl = "$url/events"
    val serverSetup = RpcServerSetup(serverHandler, generatedClass, server, format)
    return ClientServerExtension(serverSetup, generatedClient.build(client.build(url, websocketUrl), format))
}

public inline fun <reified API, I> rpcExtension(
    noinline serverHandler: (I) ->  API,
    port: Int = PortPool.get(),
    format: SerializationFormat = JsonFormat(),
    server: RpcServerEngine.MultiCall = KtorManagedRpcServer(port = port),
    client: RpcClientFactory<API> = RpcClientFactory.OkHttp()
): ClientServerExtension<API, I> {
    return rpcExtension(serverHandler, port, generatedServer(), apiClientFactory(), format, server, client)
}

@Suppress("UNCHECKED_CAST")
@PublishedApi
internal inline fun <reified API> apiClientFactory(): GeneratedClientImplFactory<API> {
    return Class.forName(GeneratedCodeUtils.Package + "." + API::class.simpleName + GeneratedCodeUtils.ClientSuffix)
        .kotlin.companionObjectInstance as GeneratedClientImplFactory<API>
}

@Suppress("UNCHECKED_CAST")
@PublishedApi
internal inline fun <reified API, Invoker> generatedServer(): GeneratedServerHelper<API, Invoker> {
    return Class.forName(GeneratedCodeUtils.Package + "." + API::class.simpleName + GeneratedCodeUtils.ServerSuffix)
        .constructors[0].newInstance() as GeneratedServerHelper<API, Invoker>
}


public class ClientServerExtension<API, I>(serverSetup: RpcServerSetup<API, RpcServerEngine.MultiCall, I>,
                                           public val api: API) : Extension, BeforeAllCallback,
    AfterAllCallback {
    private val serverExtension = MultiCallServerExtension(serverSetup)
    public val service: API = serverSetup.handler
    override fun beforeAll(context: ExtensionContext) {
        serverExtension.beforeAll(context)
    }

    override fun afterAll(context: ExtensionContext) {
        serverExtension.afterAll(context)
    }
}


/**
 * Something that creates a [RpcClient], generally there's one [RpcClientFactory] per [RpcClient]
 * This interface is useful because it's often easier to specify a [RpcClient] than an instance of a [ServerExtension] because [ServerExtension]
 * often have many parameters.
 */
public interface RpcClientFactory<API> {
    public fun build(url: String, websocketUrl: String): RpcClient

    public class OkHttp<API>(private val client: OkHttpClient = OkHttpClient()) : RpcClientFactory<API> {
        override fun build(url: String, websocketUrl: String): RpcClient = OkHttpRpcClient(url,websocketUrl, client)
    }
}
