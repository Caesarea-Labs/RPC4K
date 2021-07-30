package io.github.natanfudge.rpc4k

import io.github.natanfudge.rpc4k.impl.GeneratedClientImplSuffix
import kotlinx.coroutines.flow.Flow


internal lateinit var testRpcServer: RpcServer<*>

interface HttpClient {
    fun request(route: String, body: ByteArray) : ByteArray
    fun flowRequest(route: String, body: ByteArray): Flow<ByteArray>
}

object TestHttpClient : HttpClient {
    override fun request(route: String, body: ByteArray): ByteArray {
        return TestHttpServer.testClientHookForRequest(route, body)
    }

    override fun flowRequest(route: String, body: ByteArray): Flow<ByteArray> {
        return  TestHttpServer.testClientHookForFlowRequest(route, body)
    }

}

class RpcClient(val format: SerializationFormat, val http: HttpClient) {
    companion object {
        inline fun <reified T : Any> jvmWithProtocol(): T {
            return Class.forName(T::class.qualifiedName + GeneratedClientImplSuffix)
                .getDeclaredConstructor(RpcClient::class.java)
                .newInstance(RpcClient(JsonFormat, TestHttpClient)) as T
        }
    }

//    fun send(roa
}

class MalformedRequestException(message: String) : Exception(message)

