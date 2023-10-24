package io.github.natanfudge.rpc4k.test.util

import io.github.natanfudge.rpc4k.runtime.api.GeneratedClientImplFactory
import io.github.natanfudge.rpc4k.runtime.api.GeneratedServerHandlerFactory
import io.github.natanfudge.rpc4k.runtime.api.RpcClient
import io.github.natanfudge.rpc4k.runtime.api.SerializationFormat
import io.github.natanfudge.rpc4k.runtime.api.components.JsonFormat
import io.github.natanfudge.rpc4k.runtime.api.components.OkHttpRpcClient
import io.github.natanfudge.rpc4k.runtime.implementation.GeneratedCodeUtils
import okhttp3.OkHttpClient
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.ExtensionContext
import kotlin.reflect.full.companionObjectInstance
//io.github.natanfudge.rpc4k.test.util.KtorServerExtension@37a64f9d

fun <API> rpcExtension(
    serverHandler: API,
    generatedClass: GeneratedServerHandlerFactory<API>,
    generatedClient: GeneratedClientImplFactory<API>,
    format: SerializationFormat = JsonFormat(),
    server: ServerExtensionFactory<API> = ServerExtensionFactory.Ktor(),
    client: RpcClientFactory<API> = RpcClientFactory.OkHttp()
): ClientServerExtension<API> {
    val serverExtension = server.build(serverHandler, generatedClass, format)
    val url = "http://localhost:${serverExtension.port}"
    return ClientServerExtension(serverExtension, generatedClient.build(client.build(url), format))
}

inline fun <reified API> rpcExtension(
    serverHandler: API,
    format: SerializationFormat = JsonFormat(),
    server: ServerExtensionFactory<API> = ServerExtensionFactory.Ktor(),
    client: RpcClientFactory<API> = RpcClientFactory.OkHttp()
): ClientServerExtension<API> {
    return rpcExtension(serverHandler, apiServerFactory(), apiClientFactory(), format, server, client)
}

@Suppress("UNCHECKED_CAST")
inline fun <reified API> apiServerFactory(): GeneratedServerHandlerFactory<API> {
    return Class.forName(GeneratedCodeUtils.Package + "." + API::class.simpleName + GeneratedCodeUtils.ServerSuffix)
        .kotlin.companionObjectInstance as GeneratedServerHandlerFactory<API>
}

@Suppress("UNCHECKED_CAST")
inline fun <reified API> apiClientFactory(): GeneratedClientImplFactory<API> {
    return Class.forName(GeneratedCodeUtils.Package + "." + API::class.simpleName + GeneratedCodeUtils.ClientSuffix)
        .kotlin.companionObjectInstance as GeneratedClientImplFactory<API>
}


class ClientServerExtension<API>(private val serverExtension: ServerExtension, val api: API) : Extension, BeforeAllCallback,
    AfterAllCallback {
    override fun beforeAll(context: ExtensionContext) {
        serverExtension.beforeAll(context)
    }

    override fun afterAll(context: ExtensionContext) {
        serverExtension.afterAll(context)
    }
}

/**
 * Something that creates a [ServerExtension], generally there's one [ServerExtensionFactory] per [ServerExtension]
 * This interface is useful because it's often easier to specify a [ServerExtensionFactory] than an instance of a [ServerExtension] because [ServerExtension]
 * often have many parameters.
 */
interface ServerExtensionFactory<API> {
    fun build(api: API, generatedClassFactory: GeneratedServerHandlerFactory<API>, format: SerializationFormat): ServerExtension

    class Ktor<Api> : ServerExtensionFactory<Api> {
        override fun build(api: Api, generatedClassFactory: GeneratedServerHandlerFactory<Api>, format: SerializationFormat): ServerExtension {
            return KtorServerExtension { generatedClassFactory.build(api, format, it) }
        }
    }
}

/**
 * Something that creates a [RpcClient], generally there's one [RpcClientFactory] per [RpcClient]
 * This interface is useful because it's often easier to specify a [RpcClient] than an instance of a [ServerExtension] because [ServerExtension]
 * often have many parameters.
 */
interface RpcClientFactory<API> {
    fun build(url: String): RpcClient

    class OkHttp<API>(private val client: OkHttpClient = OkHttpClient()) : RpcClientFactory<API> {
        override fun build(url: String): RpcClient = OkHttpRpcClient(url, client)
    }
}

/**
 * Server that binds to the JUnit API to set itself up before tests and tear itself down after tests
 */
interface ServerExtension : Extension, BeforeAllCallback, AfterAllCallback {
    val port: Int
}