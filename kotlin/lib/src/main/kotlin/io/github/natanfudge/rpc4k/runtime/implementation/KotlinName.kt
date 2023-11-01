package io.github.natanfudge.rpc4k.runtime.implementation

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

sealed interface KotlinName {
    val simple: String
    val pkg: String
}

data class KotlinClassName(override val simple: String, override val pkg: String) : KotlinName {
    override fun toString(): String = "$pkg.$simple"
}


@Serializable
data class KotlinMethodName(override val simple: String, override val pkg: String) : KotlinName


val KotlinClassName.kotlinPoet get() = ClassName(pkg, simple.split("."))
val KotlinMethodName.kotlinPoet get() = MemberName(pkg, simple)

val KotlinClassName.isUnit get() = simple == "Unit" && pkg == "kotlin"

val KClass<*>.kotlinName: KotlinClassName
    get() {
        val className = asClassName()
        return KotlinClassName(simple = className.simpleNames.joinToString("."), pkg = className.packageName)
    }


/**
 * Only serializes the name of the [KotlinClassName], so just a string
 */
class SimpleNameOnlyKotlinNameSerializer : KSerializer<KotlinClassName> {
    override val descriptor: SerialDescriptor = String.serializer().descriptor

    override fun deserialize(decoder: Decoder): KotlinClassName {
        TODO()
    }

    override fun serialize(encoder: Encoder, value: KotlinClassName) {
        encoder.encodeString(value.simple)
    }
}