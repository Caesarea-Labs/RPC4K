package io.github.natanfudge.rpc4k.runtime.api.old.client

import io.github.natanfudge.rpc4k.runtime.api.old.format.JsonFormat
import io.github.natanfudge.rpc4k.runtime.impl.RpcClientComponents
import io.github.natanfudge.rpc4k.processor.old.GeneratedClientImplSuffix
import io.github.natanfudge.rpc4k.runtime.api.old.format.SerializationFormat
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass


interface RpcHttpClient {
    suspend fun request(route: String, body: ByteArray): ByteArray
    suspend fun flowRequest(route: String, body: ByteArray): Flow<ByteArray>
}

object JvmProtocolFactory {
    inline fun <reified T : Any> create(
        http: RpcHttpClient,
        format: SerializationFormat = JsonFormat,
    ) = create(http,  T::class, format)

    @Suppress("unchecked_cast")
     fun < T : Any> create(
        http: RpcHttpClient,
        protocolClass: KClass<T>,
        format: SerializationFormat = JsonFormat,
    ): T {
        return Class.forName(protocolClass.qualifiedName + GeneratedClientImplSuffix)
            .getDeclaredConstructor(RpcClientComponents::class.java)
            .newInstance(RpcClientComponents(format, http)) as T
    }
}

