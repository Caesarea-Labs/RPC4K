package io.github.natanfudge.rpc4k.runtime.api
import io.github.natanfudge.rpc4k.runtime.implementation.HeterogeneousListSerializer
import io.github.natanfudge.rpc4k.runtime.implementation.VoidUnitSerializer
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule


/**
 * For example [Json] or Protobuf
 * Must use the [Rpc4kSerializersModule]
 */
interface SerializationFormat {
    fun <T> encode(serializer: SerializationStrategy<T>, value: T): ByteArray
    fun <T> decode(serializer: DeserializationStrategy<T>, raw: ByteArray): T
}

val Rpc4kSerializersModule = SerializersModule {
    // Serialize Pair, Triple and Map.Entry as heterogeneous lists
    contextual(Pair::class) { HeterogeneousListSerializer(it) }
    contextual(Triple::class) { HeterogeneousListSerializer(it) }
    contextual(Map.Entry::class) { HeterogeneousListSerializer(it) }
    contextual(Unit::class, VoidUnitSerializer())
}