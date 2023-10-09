package io.github.natanfudge.rpc4k.test.util

import io.github.natanfudge.rpc4k.runtime.api.RpcClient
import io.github.natanfudge.rpc4k.runtime.api.SerializationFormat
import io.github.natanfudge.rpc4k.runtime.api.components.JsonFormat
import io.github.natanfudge.rpc4k.runtime.api.components.OkHttpRpcClient
import io.github.natanfudge.rpc4k.runtime.implementation.GeneratedServerHandlerFactory
import okhttp3.OkHttpClient
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.ExtensionContext

//TODO:
// Add .client(a,b) and .server(a,b,c) extension methods to reference the generated classes (this makes it more resilient to name changes)
// Add .ClientFactory and .ServerFactory objects to reference the constructors of the generated classes (this makes it easier to have generic stuff)

fun <API> rpcExtension(
    api: API,
    generatedClass: GeneratedServerHandlerFactory<API>,
    format: SerializationFormat = JsonFormat(),
    server: ServerExtensionFactory<API> = ServerExtensionFactory.Ktor(),
    client: RpcClientFactory<API> = RpcClientFactory.OkHttp()
): ClientServerExtension {
    val serverExtension = server.build(api, generatedClass, format)
    val url = "http://localhost:${serverExtension.port}"
    return ClientServerExtension(server.build(api, generatedClass, format), client.build(url))
}

inline fun <reified API> rpcExtension(
    api: API,
    format: SerializationFormat = JsonFormat(),
    server: ServerExtensionFactory<API> = ServerExtensionFactory.Ktor(),
    client: RpcClientFactory<API> = RpcClientFactory.OkHttp()
): ClientServerExtension = rpcExtension(api, apiServerFactory(), format, server, client)

inline fun <reified API> apiServerFactory(): GeneratedServerHandlerFactory<API> = TODO()

class ClientServerExtension(private val serverExtension: ServerExtension, private val client: RpcClient) : Extension, BeforeAllCallback,
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