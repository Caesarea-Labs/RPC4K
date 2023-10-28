package io.github.natanfudge.rpc4k.runtime.api
import io.github.natanfudge.rpc4k.runtime.implementation.serializers.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.time.Instant
import java.time.ZonedDateTime


/**
 * For example [Json] or Protobuf
 * Must use the [Rpc4kSerializersModule]
 */
interface SerializationFormat {
    fun <T> encode(serializer: SerializationStrategy<T>, value: T): ByteArray
    fun <T> decode(serializer: DeserializationStrategy<T>, raw: ByteArray): T
}

//
//val Rpc4kSerializersModule = SerializersModule {
//    // Serialize Pair, Triple and Map.Entry as heterogeneous lists
//    contextual(Pair::class) { TuplePairSerializer(it[0],it[1]) }
//    contextual(Triple::class) { TupleTripleSerializer(it[0], it[1], it[2]) }
//    contextual(Map.Entry::class) { TupleMapEntrySerializer(it[0], it[1]) }
//    contextual(Unit::class, VoidUnitSerializer())
//    contextual(Instant::class, InstantSerializer())
//    contextual(ZonedDateTime::class, ZonedDateTimeSerializer())
//}
//
