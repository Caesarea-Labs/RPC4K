package io.github.natanfudge.rpc4k

import io.github.natanfudge.rpc4k.impl.GeneratedClientImplSuffix


internal lateinit var testRpcServer: RpcServer<*>

class RpcClient(val format: SerializationFormat) {
    companion object {
        inline fun <reified T : Any> jvmWithProtocol(): T {
            return Class.forName(T::class.qualifiedName + GeneratedClientImplSuffix)
                .getDeclaredConstructor(RpcClient::class.java)
                .newInstance(RpcClient(JsonFormat)) as T
        }
    }

    fun send(route: String, body: ByteArray): ByteArray {
        return testRpcServer.accept(route, body)
    }
}

class MalformedRequestException(message: String) : Exception(message)

