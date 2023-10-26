@file:OptIn(ExperimentalUnsignedTypes::class)

package io.github.natanfudge.rpc4k.processor.utils

import com.google.devtools.ksp.symbol.KSClassifierReference
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.ksp.toClassName
import io.github.natanfudge.rpc4k.processor.KotlinTypeReference
import io.github.natanfudge.rpc4k.runtime.implementation.TuplePairSerializer
import io.github.natanfudge.rpc4k.runtime.implementation.TupleTripleSerializer
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZonedDateTime

/**
 * Easier representation of a KSerializer that differentiates between different types.
 */
sealed interface KotlinSerializer {
    val isNullable: Boolean
    val typeArguments: List<KotlinSerializer>


    /**
     * Serializers of the form `MyClass.serializer()` where `MyClass` is annotated by `@Serializable`
     */
    data class User(override val className: String, override val typeArguments: List<KotlinSerializer>, override val isNullable: Boolean) :
        ClassBasedKotlinSerializer


    /**
     * Serializers like `String.serializer()`, `Int.serializer()`
     */
    data class BuiltinExtension(override val className: String, override val isNullable: Boolean) : ClassBasedKotlinSerializer {
        // The builtin ones don't accept any params
        override val typeArguments: List<KotlinSerializer> = listOf()
    }

    /**
     * Serializers like `ListSerializer()`, `SetSerializer()`
     */
    data class BuiltinToplevel(
        val functionName: String, override val typeArguments: List<KotlinSerializer>, override val isNullable: Boolean
    ) : KotlinSerializer

    /**
     * Serializers like [TuplePairSerializer], [TupleTripleSerializer]
     */
    data class Rpc4KTopLevel(
        val functionName: String, override val typeArguments: List<KotlinSerializer>, override val isNullable: Boolean
    ) : KotlinSerializer
}


/**
 * [KotlinSerializer.User] and [KotlinSerializer.BuiltinExtension], basically.
 */
sealed interface ClassBasedKotlinSerializer : KotlinSerializer {
    val className: String
}


internal fun KSType.isSerializable() = isBuiltinSerializableType() || isAnnotatedBySerializable()

private fun KSType.isAnnotatedBySerializable() =
    declaration.annotations.any { it.annotationType.resolve().declaration.nonNullQualifiedName() == serializableClassName }



fun KotlinTypeReference.isBuiltinSerializableType() = qualifiedName in builtinSerializableTypes

fun KSType.isBuiltinSerializableType() = declaration.qualifiedName?.asString() in builtinSerializableTypes

internal fun KotlinTypeReference.getKSerializer(): KotlinSerializer {
    return if (isBuiltinSerializableType()) {
        // Serializers like MapSerializer
        when (qualifiedName) {
            // Unit has a special serializer
            Unit::class.qualifiedName  -> rpc4kSerializerMethod("VoidUnitSerializer")
            ZonedDateTime::class.qualifiedName  -> rpc4kSerializerMethod("ZonedDateTimeSerializer")
            Instant::class.qualifiedName -> rpc4kSerializerMethod("InstantSerializer")
            in classesWithSeparateBuiltinSerializerMethod -> topLevelSerializerMethod()
            in classesWithTupleSerializerMethod -> tupleRpc4kSerializerMethod()
            else -> builtinExtensionSerializerMethod()
        }
    } else {
        userClassSerializer()
    }
}



/**
 * Gets serializers like ListSerializer, SetSerializer, etc
 */
private fun KotlinTypeReference.topLevelSerializerMethod(): KotlinSerializer {
    // For example, for the name Map.Entry we get MapEntrySerializer
    return KotlinSerializer.BuiltinToplevel(
        functionName = "${simpleName}Serializer",
        typeArguments = typeArguments.map { it.getKSerializer() },
        isNullable
    )
}

/**
 * Gets serializers like ListSerializer, SetSerializer, etc
 */
private fun KotlinTypeReference.tupleRpc4kSerializerMethod(): KotlinSerializer {
    // Map.Entry needs special handling to get the correct serializer
    val name = simpleName.let { if (it == "Map.Entry") "MapEntry" else it }
    // For example, for the name Map.Entry we get MapEntrySerializer
    return rpc4kSerializerMethod("Tuple${name}Serializer")

}
/**
 * Gets serializers like ListSerializer, SetSerializer, etc
 */
private fun KotlinTypeReference.rpc4kSerializerMethod(name: String): KotlinSerializer {
    // For example, for the name Map.Entry we get MapEntrySerializer
    return KotlinSerializer.Rpc4KTopLevel(
        functionName = name,
        typeArguments = typeArguments.getKSerializers(),
        isNullable
    )
}

private fun KotlinTypeReference.builtinExtensionSerializerMethod(): KotlinSerializer.BuiltinExtension {
    return KotlinSerializer.BuiltinExtension(className = qualifiedName, isNullable)
}


private fun KotlinTypeReference.userClassSerializer() = KotlinSerializer.User(
    className = qualifiedName,
    typeArguments = typeArguments.getKSerializers(),
    isNullable
)

private fun List<KotlinTypeReference>.getKSerializers() = map { it.getKSerializer() }


/**
 * Gets serializers like ListSerializer, SetSerializer, etc
 */
//private fun RpcType.topLevelSerializerMethod(): FormattedString {
//    // Map.Entry needs special handling to get the correct serializer
//    val name = simpleName.let { if (it == "Map.Entry") "MapEntry" else it }
//    // For example, for the name Map.Entry we get MapEntrySerializer
//    val serializerMethod = MemberName("kotlinx.serialization.builtins", "${name}Serializer")
//    return serializerMethod.withSerializerArguments(typeArguments)
//}

//private fun RpcType.builtinExtensionSerializerMethod(): FormattedString {
//    return "%T.serializer()".formatWith(className)
//}


/**
 * These classes don't have a T.serializer() for some reason but instead have a separate top-level method
 */
@OptIn(ExperimentalUnsignedTypes::class)
private val classesWithSeparateBuiltinSerializerMethod = listOf(
    List::class, Set::class, Map::class,
    ByteArray::class, ShortArray::class, IntArray::class, LongArray::class, CharArray::class,
    Array::class, UByteArray::class, UShortArray::class, UIntArray::class, ULongArray::class
).map { it.qualifiedName }.toHashSet()

private val classesWithTupleSerializerMethod = listOf(Pair::class, Map.Entry::class, Triple::class).map { it.qualifiedName }.toHashSet()


// Nullability is handled separately
//private fun RpcType.userClassSerializer() = "%T.serializer".formatWith(className)
//    .withMethodSerializerArguments(typeArguments)


private val serializableClassName = Serializable::class.qualifiedName

@ExperimentalUnsignedTypes
private val builtinSerializableTypes = listOf(
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
    Instant::class,
    ZonedDateTime::class
).map { it.qualifiedName!! }.toHashSet()


