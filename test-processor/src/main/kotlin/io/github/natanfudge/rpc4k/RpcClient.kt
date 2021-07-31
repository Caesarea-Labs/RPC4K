package io.github.natanfudge.rpc4k

import io.github.natanfudge.rpc4k.impl.GeneratedClientImplSuffix
import kotlinx.coroutines.flow.Flow


internal lateinit var testRpcServer: RpcServer<*>

interface HttpClient {
    fun request(route: String, body: ByteArray): ByteArray
    fun flowRequest(route: String, body: ByteArray): Flow<ByteArray>
}



class RpcClient(val format: SerializationFormat, val http: HttpClient) {
    companion object {
        inline fun <reified T : Any> jvmWithProtocol(format: SerializationFormat = JsonFormat, http: HttpClient): T {
            return Class.forName(T::class.qualifiedName + GeneratedClientImplSuffix)
                .getDeclaredConstructor(RpcClient::class.java)
                .newInstance(RpcClient(format, http)) as T
        }
    }
}



