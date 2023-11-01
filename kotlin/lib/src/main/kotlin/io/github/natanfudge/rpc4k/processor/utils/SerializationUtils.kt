@file:OptIn(ExperimentalUnsignedTypes::class)

package io.github.natanfudge.rpc4k.processor.utils

import com.google.devtools.ksp.symbol.KSType
import io.github.natanfudge.rpc4k.processor.KotlinTypeReference
import io.github.natanfudge.rpc4k.runtime.implementation.KotlinClassName
import io.github.natanfudge.rpc4k.runtime.implementation.KotlinMethodName
import io.github.natanfudge.rpc4k.runtime.implementation.kotlinName
import io.github.natanfudge.rpc4k.runtime.implementation.serializers.Rpc4kSerializer
import io.github.natanfudge.rpc4k.runtime.implementation.serializers.Rpc4kSerializers
import kotlinx.serialization.Serializable
import kotlin.time.Duration

/**
 * Easier representation of a KSerializer that differentiates between different types.
 */
sealed interface KotlinSerializer {
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
    declaration.annotations.any { it.annotationType.resolve().declaration.nonNullQualifiedName() == serializableClassName }


fun KSType.isBuiltinSerializableType() = declaration.qualifiedName?.asString() in builtinSerializableClasses

private val rpc4kSerializerMap = Rpc4kSerializers.associateBy {
    it.kClass.kotlinName
}

internal fun KotlinTypeReference.getKSerializer(): KotlinSerializer {
    val rpc4kSerializer = rpc4kSerializerMap[name]
    return if (rpc4kSerializer != null) {
        when (rpc4kSerializer) {
            is Rpc4kSerializer.Object -> KotlinSerializer.Object(rpc4kSerializer.name, isNullable)
            is Rpc4kSerializer.Function -> KotlinSerializer.TopLevelFunction(
                rpc4kSerializer.name,
                isNullable,
                typeArguments.map { it.getKSerializer() })
        }
    } else if (name in classesWithSeparateKxsBuiltinSerializerMethod) {
        //TODO: this check is not working
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
    // For example, for the name Map.Entry we get MapEntrySerializer
    return KotlinSerializer.TopLevelFunction(
        name = KotlinMethodName(
            simple = "${name.simple}Serializer",
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
private val classesWithSeparateKxsBuiltinSerializerMethod = listOf(
    List::class, Set::class, Map::class,
    ByteArray::class, ShortArray::class, IntArray::class, LongArray::class, CharArray::class,
    Array::class, UByteArray::class, UShortArray::class, UIntArray::class, ULongArray::class
).map { it.kotlinName }.toHashSet()

private val serializableClassName = Serializable::class.qualifiedName

@ExperimentalUnsignedTypes
private val builtinSerializableClasses = (listOf(
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
    Set::class,
    List::class,
    Pair::class,
    Triple::class,
    Map.Entry::class,
    Map::class,
    ByteArray::class,
    ShortArray::class,
    IntArray::class,
    LongArray::class,
    CharArray::class,
    Array::class,
    UByteArray::class,
    UShortArray::class,
    UIntArray::class,
    ULongArray::class,
    UByte::class,
    UShort::class,
    UInt::class,
    ULong::class,
    Duration::class,
// Add classes that rpc4k supports as well
) + Rpc4kSerializers.map { it.kClass }).map { it.qualifiedName!! }.toHashSet()


