package com.caesarealabs.rpc4k.runtime.user

import com.benasher44.uuid.Uuid
import com.caesarealabs.rpc4k.runtime.implementation.serializers.UUIDSerializer
import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.serialization.Serializable

public typealias SerializableUUID = @Serializable(with = UUIDSerializer::class) Uuid