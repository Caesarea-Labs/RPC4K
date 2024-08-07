package com.caesarealabs.rpc4k.runtime.api
import com.caesarealabs.rpc4k.runtime.implementation.serializers.*
import io.ktor.http.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json


/**
 * For example [Json] or Protobuf
 * Must use the [Rpc4kSerializersModule]
 */
public interface SerializationFormat {
    public fun <T> encode(serializer: SerializationStrategy<T>, value: T): ByteArray
    public fun <T> decode(serializer: DeserializationStrategy<T>, raw: ByteArray): T
    public val contentType: ContentType
}

