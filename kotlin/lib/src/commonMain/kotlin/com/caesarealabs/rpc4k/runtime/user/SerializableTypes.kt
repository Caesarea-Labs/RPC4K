package com.caesarealabs.rpc4k.runtime.user

import com.benasher44.uuid.Uuid
import com.caesarealabs.rpc4k.runtime.implementation.serializers.UUIDSerializer
import kotlinx.serialization.Serializable

public typealias UUID = @Serializable(with = UUIDSerializer::class) Uuid