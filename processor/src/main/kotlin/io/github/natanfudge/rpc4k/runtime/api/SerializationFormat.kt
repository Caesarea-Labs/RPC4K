package io.github.natanfudge.rpc4k.runtime.api
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json


/**
 * For example [Json] or Protobuf
 */
interface SerializationFormat {
    fun <T> encode(serializer: SerializationStrategy<T>, value: T): ByteArray
    fun <T> decode(serializer: DeserializationStrategy<T>, raw: ByteArray): T

    /**
     * Formats generally have a way of combining multiple values into a list of these values.
     * For example JSON has lists: [value1, value2, value3]
     */
//    fun combine(values: List<ByteArray>): ByteArray
}