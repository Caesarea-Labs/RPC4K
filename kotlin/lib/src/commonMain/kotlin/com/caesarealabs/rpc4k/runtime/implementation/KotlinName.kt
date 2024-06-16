package com.caesarealabs.rpc4k.runtime.implementation


import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.ArrayDeque
import kotlin.reflect.KClass

public sealed interface KotlinName {
     public val simple: String
     public val pkg: String
}

public data class KotlinClassName(override val simple: String, override val pkg: String) : KotlinName {
    override fun toString(): String = "$pkg.$simple"

    public companion object {
        public fun ofKClass(kclass: KClass<*>): KotlinClassName  {
            // Taken from KotlinPoet
            var qualifiedName = requireNotNull(kclass.qualifiedName) { "$this cannot be represented as a KotlinName" }

            // First, check for Kotlin types whose enclosing class name is a type that is mapped to a JVM
            // class. Thus, the class backing the nested Kotlin type does not have an enclosing class
            // (i.e., a parent) and the normal algorithm will fail.
            val names = when (qualifiedName) {
                "kotlin.Boolean.Companion" -> listOf("kotlin", "Boolean", "Companion")
                "kotlin.Byte.Companion" -> listOf("kotlin", "Byte", "Companion")
                "kotlin.Char.Companion" -> listOf("kotlin", "Char", "Companion")
                "kotlin.Double.Companion" -> listOf("kotlin", "Double", "Companion")
                "kotlin.Enum.Companion" -> listOf("kotlin", "Enum", "Companion")
                "kotlin.Float.Companion" -> listOf("kotlin", "Float", "Companion")
                "kotlin.Int.Companion" -> listOf("kotlin", "Int", "Companion")
                "kotlin.Long.Companion" -> listOf("kotlin", "Long", "Companion")
                "kotlin.Short.Companion" -> listOf("kotlin", "Short", "Companion")
                "kotlin.String.Companion" -> listOf("kotlin", "String", "Companion")
                else -> {
                    val names = ArrayDeque<String>()
                    var target: Class<*>? = kclass.java
                    while (target != null) {
                        target = target.enclosingClass

                        val dot = qualifiedName.lastIndexOf('.')
                        if (dot == -1) {
                            if (target != null) throw AssertionError(this) // More enclosing classes than dots.
                            names.addFirst(qualifiedName)
                            qualifiedName = ""
                        } else {
                            names.addFirst(qualifiedName.substring(dot + 1))
                            qualifiedName = qualifiedName.substring(0, dot)
                        }
                    }

                    names.addFirst(qualifiedName)
                    names.toList()
                }
            }
            return KotlinClassName(simple = names.drop(1) .joinToString("."), pkg = names[0])
        }
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


