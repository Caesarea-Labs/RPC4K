package io.github.natanfudge.rpc4k

import io.github.natanfudge.rpc4k.impl.GeneratedClientImplSuffix


internal lateinit var testRpcServer: RpcServer<*>

class RpcClient {
    companion object {
        inline fun <reified T : Any> jvmWithProtocol(): T {
            return Class.forName(T::class.qualifiedName + GeneratedClientImplSuffix)
                .getDeclaredConstructor(RpcClient::class.java)
                .newInstance(RpcClient()) as T
        }
    }

    fun send(route: String, body: String): String {
        return testRpcServer.accept(route, body)
    }
}

class MalformedRequestException(message: String) : Exception(message)

