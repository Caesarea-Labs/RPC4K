@file:Suppress("FunctionName")

package io.github.natanfudge.rpc4k.runtime.implementation

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * The RPC4All spec defines the value of the void/unit type as a "void" string.
 */
fun VoidUnitSerializer(): KSerializer<Unit> = VoidUnitSerializerInstance

private object VoidUnitSerializerInstance : KSerializer<Unit> {
    override val descriptor: SerialDescriptor = String.serializer().descriptor

    override fun deserialize(decoder: Decoder) {
        // Need to do this call or some formats fail
        decoder.decodeString()
    }

    override fun serialize(encoder: Encoder, value: Unit) {
        encoder.encodeString("void")
    }
}