package io.github.natanfudge.rpc4k

import io.github.natanfudge.rpc4k.impl.GeneratedServerImplSuffix
import io.github.natanfudge.rpc4k.impl.json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray

interface ProtocolDecoder<T> {
    fun accept(route: String, args: List<JsonElement>): String
}

private const val devChecks = true

private inline fun devCheck(check: () -> Boolean) {
    if (devChecks && !check()) throw IllegalStateException()
}

class RpcServer<T> private constructor(private val decoder: ProtocolDecoder<T>) {
    companion object {
        fun <T> start(decoder: ProtocolDecoder<T>): RpcServer<T> {
            return RpcServer(decoder).apply { start() }
        }

        inline fun <reified T : Any> jvmStartWithProtocol(protocolImpl: T): RpcServer<T> {
            val protocolClass = T::class
            val decoder = Class.forName(protocolClass.qualifiedName + GeneratedServerImplSuffix)
                .getDeclaredConstructor(protocolClass.java).newInstance(protocolImpl) as ProtocolDecoder<T>
            return start(decoder)
        }
    }

    private fun start() {
        testRpcServer = this
    }

    internal fun accept(route: String, body: String): String {
        return decoder.accept(route, json.parseToJsonElement("[$body]").jsonArray)
    }
}