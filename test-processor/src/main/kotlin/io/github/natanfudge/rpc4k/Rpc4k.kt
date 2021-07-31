package io.github.natanfudge.rpc4k

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy

interface SerializationFormat {
    fun <T> encode(serializer: SerializationStrategy<T>, value: T): ByteArray
    fun <T> decode(serializer: DeserializationStrategy<T>, raw: ByteArray): T
}


class MalformedRequestException(message: String) : Exception(message)