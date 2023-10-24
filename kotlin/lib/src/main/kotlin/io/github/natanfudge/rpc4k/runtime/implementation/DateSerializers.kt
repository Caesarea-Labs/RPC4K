@file:Suppress("FunctionName")

package io.github.natanfudge.rpc4k.runtime.implementation

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.time.ZonedDateTime

/**
 * We serialized instant as an iso string
 */
fun InstantSerializer(): KSerializer<Instant> = InstantIsoSerializer
/**
 * We serialized ZonedDateTime as an iso string
 */
fun ZonedDateTimeSerializer(): KSerializer<ZonedDateTime> = ZonedDateTimeIsoSerializer

private object InstantIsoSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = String.serializer().descriptor

    override fun deserialize(decoder: Decoder): Instant = Instant.parse(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeString(value.toString())
}



private object ZonedDateTimeIsoSerializer : KSerializer<ZonedDateTime> {
    override val descriptor: SerialDescriptor = String.serializer().descriptor

    override fun deserialize(decoder: Decoder): ZonedDateTime = ZonedDateTime.parse(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: ZonedDateTime) = encoder.encodeString(value.toString())
}