@file:Suppress("FunctionName")

package com.caesarealabs.rpc4k.runtime.implementation.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

//TODO: use multiplatform Instant instead

//import java.time.Instant
//import java.time.ZonedDateTime
//
//
//
///**
// * We serialize instant as an iso string
// */
//public object InstantIsoSerializer : KSerializer<Instant> {
//    override val descriptor: SerialDescriptor = String.serializer().descriptor
//
//    override fun deserialize(decoder: Decoder): Instant = Instant.parse(decoder.decodeString())
//
//    override fun serialize(encoder: Encoder, value: Instant): Unit = encoder.encodeString(value.toString())
//}
//
//
///**
// * We serialize ZonedDateTime as an iso string
// */
//public object ZonedDateTimeIsoSerializer : KSerializer<ZonedDateTime> {
//    override val descriptor: SerialDescriptor = String.serializer().descriptor
//
//    override fun deserialize(decoder: Decoder): ZonedDateTime = ZonedDateTime.parse(decoder.decodeString())
//
//    override fun serialize(encoder: Encoder, value: ZonedDateTime): Unit = encoder.encodeString(value.toString())
//}