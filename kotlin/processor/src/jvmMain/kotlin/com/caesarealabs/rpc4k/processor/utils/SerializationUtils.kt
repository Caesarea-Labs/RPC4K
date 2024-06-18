@file:OptIn(ExperimentalUnsignedTypes::class)

package com.caesarealabs.rpc4k.processor.utils

import com.google.devtools.ksp.symbol.KSType
import com.caesarealabs.rpc4k.processor.KotlinTypeReference
import com.caesarealabs.rpc4k.processor.utils.poet.kotlinName
import com.caesarealabs.rpc4k.runtime.implementation.KotlinClassName
import com.caesarealabs.rpc4k.runtime.implementation.KotlinMethodName
import com.caesarealabs.rpc4k.runtime.implementation.serializers.Rpc4kSerializer
import com.caesarealabs.rpc4k.runtime.implementation.serializers.Rpc4kSerializers
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Duration

/**
 * Easier representation of a KSerializer that differentiates between different types.
 */
internal sealed interface KotlinSerializer {
    val isNullable: Boolean
    val typeArguments: List<KotlinSerializer>

    /**
     * Serializers like `String.serializer()`, `Int.serializer()`
     * also:  `MyClass.serializer()` where `MyClass` is annotated by `@Serializable`
     */
    data class CompanionExtension(
        val className: KotlinClassName,
        override val isNullable: Boolean,
        override val typeArguments: List<KotlinSerializer>
    ) : KotlinSerializer

    /**
     * Serializers like `ListSerializer()`, `SetSerializer()`
     */
    data class TopLevelFunction(
        val name: KotlinMethodName,
        override val isNullable: Boolean,
        override val typeArguments: List<KotlinSerializer>
    ) : KotlinSerializer

    /**
     * Serializers like UUIDSerializer
     */
    data class Object(val name: KotlinClassName, override val isNullable: Boolean) : KotlinSerializer {
        override val typeArguments: List<KotlinSerializer> = listOf()
    }
}


internal fun KSType.isSerializable() = isBuiltinSerializableType() || isAnnotatedBySerializable()

private fun KSType.isAnnotatedBySerializable() =
    declaration.annotations.any { it.annotationType.resolve().declaration.getQualifiedName() == serializableClassName }


internal fun KSType.isBuiltinSerializableType() = declaration.qualifiedName?.asString() in builtinSerializableClasses

private val rpc4kSerializerMap = Rpc4kSerializers.associateBy {
    it.serializedName
}

internal fun KotlinTypeReference.getKSerializer(): KotlinSerializer {
    val rpc4kSerializer = rpc4kSerializerMap[name]
    return if (rpc4kSerializer != null) {
        when (rpc4kSerializer) {
            is Rpc4kSerializer.Object -> KotlinSerializer.Object(rpc4kSerializer.serializerName, isNullable)
            is Rpc4kSerializer.Function -> KotlinSerializer.TopLevelFunction(
                rpc4kSerializer.serializerName,
                isNullable,
                typeArguments.map { it.getKSerializer() })
        }
    } else if (name in classesWithSeparateKxsBuiltinSerializerMethod) {
        kxsTopLevelSerializerMethod()
    } else {
        KotlinSerializer.CompanionExtension(
            className = name,
            isNullable,
            typeArguments = typeArguments.getKSerializers(),
        )
    }

}


/**
 * Gets serializers like ListSerializer, SetSerializer, etc
 */
private fun KotlinTypeReference.kxsTopLevelSerializerMethod(): KotlinSerializer {
    // MutableX classes use the normal readonly serializers
    val namePrefix = name.simple.removePrefix("Mutable")
    // For example, for the name Map.Entry we get MapEntrySerializer
    return KotlinSerializer.TopLevelFunction(
        name = KotlinMethodName(
            simple = "${namePrefix}Serializer",
            pkg = "kotlinx.serialization.builtins"
        ),
        isNullable,
        typeArguments = typeArguments.map { it.getKSerializer() }
    )
}


private fun List<KotlinTypeReference>.getKSerializers() = map { it.getKSerializer() }

/**
 * These classes don't have a T.serializer() for some reason but instead have a separate top-level method
 */
@OptIn(ExperimentalUnsignedTypes::class)
private val classesWithSeparateKxsBuiltinSerializerMethod: Set<KotlinClassName> = listOf(
    List::class, Set::class, Map::class,
    ByteArray::class, ShortArray::class, IntArray::class, LongArray::class, CharArray::class,
    Array::class, UByteArray::class, UShortArray::class, UIntArray::class, ULongArray::class
).map { it.kotlinName }.toHashSet() +
        listOf(KotlinClassName.MutableMap, KotlinClassName.MutableList, KotlinClassName.MutableSet)

private val serializableClassName = Serializable::class.qualifiedName




@ExperimentalUnsignedTypes
internal val builtinSerializableClasses: Set<String> = (listOf(
    Byte::class,
    Short::class,
    Int::class,
    Long::class,
    Float::class,
    Double::class,
    Boolean::class,
    Unit::class,
    String::class,
    Char::class,
    UInt::class,
    ULong::class,
    UByte::class,
    UShort::class,
    Pair::class,
    Triple::class,
    Map.Entry::class,
    UByte::class,
    UShort::class,
    UInt::class,
    ULong::class,
    Duration::class,
    Instant::class,
    // We don't support this JVM UUID, but sometimes the Multiplatform UUID evaluates to this during symbol processing so we have to accept it
    UUID::class
// Add classes that rpc4k supports as well
).map { it.qualifiedName!! } + Rpc4kSerializers.map { it.serializedName.toString() }).toHashSet() +
        classesWithSeparateKxsBuiltinSerializerMethod.map { it.toString() }




