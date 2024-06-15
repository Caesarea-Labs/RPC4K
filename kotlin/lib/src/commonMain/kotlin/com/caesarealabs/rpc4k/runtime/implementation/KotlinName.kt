package com.caesarealabs.rpc4k.runtime.implementation

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.asClassName
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.reflect.KClass

internal sealed interface KotlinName {
     val simple: String
     val pkg: String
}

internal data class KotlinClassName(override val simple: String, override val pkg: String) : KotlinName {
    override fun toString(): String = "$pkg.$simple"

    companion object {
        // MutableMap::class evaluates as kotlin.collections.Map and same goes for the other mutable collections classes.
        // Therefore, we need to explicitly define these class names.
        val MutableMap: KotlinClassName = KotlinClassName("MutableMap", "kotlin.collections")
        val MutableSet: KotlinClassName = KotlinClassName("MutableSet", "kotlin.collections")
        val MutableList: KotlinClassName = KotlinClassName("MutableList", "kotlin.collections")
    }
}


@Serializable
internal data class KotlinMethodName(override val simple: String, override val pkg: String) : KotlinName


internal val KotlinClassName.kotlinPoet: ClassName get() = ClassName(pkg, simple.split("."))
internal val KotlinMethodName.kotlinPoet: MemberName get() = MemberName(pkg, simple)

internal val KotlinClassName.isUnit: Boolean get() = simple == "Unit" && pkg == "kotlin"

internal val KClass<*>.kotlinName: KotlinClassName
    get() {
        val className = asClassName()
        return KotlinClassName(simple = className.simpleNames.joinToString("."), pkg = className.packageName)
    }


/**
 * Only serializes the name of the [KotlinClassName], so just a string
 */
internal class SimpleNameOnlyKotlinNameSerializer : KSerializer<KotlinClassName> {
    override val descriptor: SerialDescriptor = String.serializer().descriptor

    override fun deserialize(decoder: Decoder): KotlinClassName {
        throw IllegalStateException("Not implemented")
    }

    override fun serialize(encoder: Encoder, value: KotlinClassName) {
        encoder.encodeString(value.simple)
    }
}