package io.github.natanfudge.rpc4k.processor.utils

import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ksp.toClassName
import io.github.natanfudge.rpc4k.processor.RpcType
import io.github.natanfudge.rpc4k.processor.utils.poet.FormattedString
import io.github.natanfudge.rpc4k.processor.utils.poet.formatWith
import io.github.natanfudge.rpc4k.processor.utils.poet.withArgumentList
import io.github.natanfudge.rpc4k.processor.utils.poet.withMethodArguments
import kotlinx.serialization.Serializable

/**
 * Easier representation of a KSerializer that differentiates between different types.
 */
sealed interface KotlinSerializer {
    val isNullable: Boolean
    val typeArguments: List<KotlinSerializer>

    /**
     * Serializers of the form `MyClass.serializer()` where `MyClass` is annotated by `@Serializable`
     */
    data class User(override val className: String, override val typeArguments: List<KotlinSerializer>, override val isNullable: Boolean):  ClassBasedKotlinSerializer


    /**
     * Serializers like `String.serializer()`, `Int.serializer()`
     */
    data class BuiltinExtension(override val className: String, override val isNullable: Boolean): ClassBasedKotlinSerializer {
        // The builtin ones don't accept any params
        override val typeArguments: List<KotlinSerializer> = listOf()
    }
    /**
     * Serializers like `ListSerializer()`, `SetSerializer()`
     */
    data class BuiltinToplevel(val functionName: String, override val typeArguments: List<KotlinSerializer>, override val isNullable: Boolean): KotlinSerializer
}

/**
 * [KotlinSerializer.User] and [KotlinSerializer.BuiltinExtension], basically.
 */
sealed interface ClassBasedKotlinSerializer: KotlinSerializer {
    val className: String
}


internal fun KSType.isSerializable() = isBuiltinSerializableType() || isAnnotatedBySerializable()

private fun KSType.isAnnotatedBySerializable() =
    declaration.annotations.any { it.annotationType.resolve().toClassName().canonicalName == serializableClassName }

fun RpcType.isBuiltinSerializableType() = qualifiedName in builtinSerializableTypes

private fun KSType.isBuiltinSerializableType() = declaration.qualifiedName?.asString() in builtinSerializableTypes

internal fun RpcType.getKSerializer(): KotlinSerializer {
    return if (isBuiltinSerializableType()) {
        if (qualifiedName in classesWithSeparateSerializerMethod) {
            // Serializers like MapSerializer
            topLevelSerializerMethod()
        } else {
            builtinExtensionSerializerMethod()
        }
    } else {
        userClassSerializer()
    }
}


/**
 * Gets serializers like ListSerializer, SetSerializer, etc
 */
private fun RpcType.topLevelSerializerMethod(): KotlinSerializer {
    // Map.Entry needs special handling to get the correct serializer
    val name = simpleName.let { if (it == "Map.Entry") "MapEntry" else it }
    // For example, for the name Map.Entry we get MapEntrySerializer
    return KotlinSerializer.BuiltinToplevel(
        functionName = "${name}Serializer",
        typeArguments = typeArguments.map { it.getKSerializer() },
        isNullable
    )
}

private fun RpcType.builtinExtensionSerializerMethod(): KotlinSerializer.BuiltinExtension {
    return KotlinSerializer.BuiltinExtension(className = qualifiedName, isNullable)
}


private fun RpcType.userClassSerializer() = KotlinSerializer.User(
    className = qualifiedName,
    typeArguments = typeArguments.map { it.getKSerializer() },
    isNullable
)



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
private val classesWithSeparateSerializerMethod = listOf(
    List::class, Set::class, Map::class, Pair::class, Map.Entry::class, Triple::class
).map { it.qualifiedName }.toHashSet()


// Nullability is handled separately
//private fun RpcType.userClassSerializer() = "%T.serializer".formatWith(className)
//    .withMethodSerializerArguments(typeArguments)



private val serializableClassName = Serializable::class.qualifiedName
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
).map { it.qualifiedName!! }.toHashSet()