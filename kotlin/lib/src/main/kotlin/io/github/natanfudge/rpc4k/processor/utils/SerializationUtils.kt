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

/**
 * Easier representation of a KSerializer that differentiates between different types.
 */
sealed interface KotlinSerializer {
    val isNullable: Boolean
    val typeArguments: List<KotlinSerializer>


//    /**
//     * Serializers of the form
//     */
//    data class User(override val className: String, override val typeArguments: List<KotlinSerializer>, override val isNullable: Boolean) :
//        ClassBasedKotlinSerializer
//
//
//
//    data class BuiltinExtension(override val className: String, override val isNullable: Boolean) : ClassBasedKotlinSerializer {
//        // The builtin ones don't accept any params
//        override val typeArguments: List<KotlinSerializer> = listOf()
//    }
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

//    /**
//     * Serializers like `ListSerializer()`, `SetSerializer()`
//     */
//    data class BuiltinToplevel(
//        val functionName: String, override val typeArguments: List<KotlinSerializer>, override val isNullable: Boolean
//    ) : KotlinSerializer
//
//    /**
//     * Serializers like [TuplePairSerializer], [TupleTripleSerializer]
//     */
//    data class Rpc4KTopLevel(
//        val functionName: String, override val typeArguments: List<KotlinSerializer>, override val isNullable: Boolean
//    ) : KotlinSerializer
//
//    data class Rpc4K
}


///**
// * [KotlinSerializer.User] and [KotlinSerializer.BuiltinExtension], basically.
// */
//sealed interface ClassBasedKotlinSerializer : KotlinSerializer {
//    val className: String
//}


internal fun KSType.isSerializable() = isBuiltinSerializableType() || isAnnotatedBySerializable()

private fun KSType.isAnnotatedBySerializable() =
    declaration.annotations.any { it.annotationType.resolve().declaration.nonNullQualifiedName() == serializableClassName }


//fun KotlinTypeReference.isBuiltinSerializableType() = name in builtinSerializableKotlinNames

fun KSType.isBuiltinSerializableType() = declaration.qualifiedName?.asString() in builtinSerializableClasses

private val rpc4kSerializerMap = Rpc4kSerializers.associateBy { it.kClass.kotlinName }

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


//    return if (isBuiltinSerializableType()) {
//        // Serializers like MapSerializer
//        when (qualifiedName) {
//
//            // Unit has a special serializer
//            Unit::class.qualifiedName -> rpc4kSerializerMethod("VoidUnitSerializer")
//            ZonedDateTime::class.qualifiedName -> rpc4kSerializerMethod("ZonedDateTimeSerializer")
//            Instant::class.qualifiedName -> rpc4kSerializerMethod("InstantSerializer")
//            in classesWithSeparateKxsBuiltinSerializerMethod -> kxsTopLevelSerializerMethod()
//            in classesWithTupleSerializerMethod -> tupleRpc4kSerializerMethod()
//            //TODO: maybe we can join this branch with another case
//            else -> builtinExtensionSerializerMethod()
//        }
//    } else {
//        userClassSerializer()
//    }
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
//
///**
// * Gets serializers like ListSerializer, SetSerializer, etc
// */
//private fun KotlinTypeReference.tupleRpc4kSerializerMethod(): KotlinSerializer {
//    // Map.Entry needs special handling to get the correct serializer
//    val name = simpleName.let { if (it == "Map.Entry") "MapEntry" else it }
//    // For example, for the name Map.Entry we get MapEntrySerializer
//    return rpc4kSerializerMethod("Tuple${name}Serializer")
//}
//
///**
// * Gets serializers like ListSerializer, SetSerializer, etc
// */
//private fun KotlinTypeReference.rpc4kSerializerMethod(name: String): KotlinSerializer {
//    // For example, for the name Map.Entry we get MapEntrySerializer
//    return KotlinSerializer.Rpc4KTopLevel(
//        functionName = name,
//        typeArguments = typeArguments.getKSerializers(),
//        isNullable
//    )
//}
//
//private fun KotlinTypeReference.builtinExtensionSerializerMethod(): KotlinSerializer.CompanionExtension {
//    return KotlinSerializer.CompanionExtension(className = poetName, isNullable, typeArguments = listOf())
//}


//private fun KotlinTypeReference.userClassSerializer() = KotlinSerializer.User(
//    className = qualifiedName,
//    typeArguments = typeArguments.getKSerializers(),
//    isNullable
//)

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
private val classesWithSeparateKxsBuiltinSerializerMethod = listOf(
    List::class, Set::class, Map::class,
    ByteArray::class, ShortArray::class, IntArray::class, LongArray::class, CharArray::class,
    Array::class, UByteArray::class, UShortArray::class, UIntArray::class, ULongArray::class
).map { it.kotlinName }.toHashSet()

//private val classesWithTupleSerializerMethod = listOf(Pair::class, Map.Entry::class, Triple::class).map { it.qualifiedName }.toHashSet()


// Nullability is handled separately
//private fun RpcType.userClassSerializer() = "%T.serializer".formatWith(className)
//    .withMethodSerializerArguments(typeArguments)


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
// Add classes that rpc4k supports as well
) + Rpc4kSerializers.map { it.kClass }).map { it.qualifiedName!! }.toHashSet()


