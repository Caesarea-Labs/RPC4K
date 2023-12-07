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

