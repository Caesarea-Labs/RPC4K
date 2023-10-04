package io.github.natanfudge.rpc4k.runtime.api.format

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy

interface SerializationFormat {
    fun <T> serialize(serializer: SerializationStrategy<T>, value: T): ByteArray
    fun <T> deserialize(serializer: DeserializationStrategy<T>, raw: ByteArray): T
}