package com.caesarealabs.rpc4k.runtime.implementation


import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.reflect.KClass

public sealed interface KotlinName {
     public val simple: String
     public val pkg: String
}

public data class KotlinClassName(override val simple: String, override val pkg: String) : KotlinName {
    override fun toString(): String = "$pkg.$simple"
    public companion object {
        // MutableMap::class evaluates as kotlin.collections.Map and same goes for the other mutable collections classes.
        // Therefore, we need to explicitly define these class names.
        public val MutableMap: KotlinClassName = KotlinClassName("MutableMap", "kotlin.collections")
        public val MutableSet: KotlinClassName = KotlinClassName("MutableSet", "kotlin.collections")
        public  val MutableList: KotlinClassName = KotlinClassName("MutableList", "kotlin.collections")
    }
}


@Serializable
public data class KotlinMethodName(override val simple: String, override val pkg: String) : KotlinName



public val KotlinClassName.isUnit: Boolean get() = simple == "Unit" && pkg == "kotlin"




/**
 * Only serializes the name of the [KotlinClassName], so just a string
 */
public class SimpleNameOnlyKotlinNameSerializer : KSerializer<KotlinClassName> {
    override val descriptor: SerialDescriptor = String.serializer().descriptor

    override fun deserialize(decoder: Decoder): KotlinClassName {
        throw IllegalStateException("Not implemented")
    }

    override fun serialize(encoder: Encoder, value: KotlinClassName) {
        encoder.encodeString(value.simple)
    }
}


