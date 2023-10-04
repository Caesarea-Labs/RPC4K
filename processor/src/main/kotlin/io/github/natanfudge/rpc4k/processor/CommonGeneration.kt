package io.github.natanfudge.rpc4k.processor

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.FileSpec
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

internal fun FileSpec.Builder.importBuiltinSerializers() {
    addImport(
        "kotlinx.serialization.builtins",
        "serializer",
        "nullable",
        "ListSerializer",
        "SetSerializer",
        "MapSerializer",
        "PairSerializer",
        "MapEntrySerializer",
        "TripleSerializer"
    )

}

internal fun KSTypeReference.isFlow() =
    resolve().declaration.qualifiedName?.asString() == Flow::class.qualifiedName

internal fun KSTypeReference.serializerString() = resolve().serializerString()

// When using flows, we only serialize the elements individually
internal fun KSTypeReference.returnTypeSerializer(): FormattedString {
    val type = if (isFlow()) resolve().arguments[0].type else this
    return type!!.serializerString()
}

internal inline fun KSNode.checkRequirement(env: SymbolProcessorEnvironment, requirement: Boolean, msg: () -> String) {
    if (!requirement) env.logger.error(msg(), this)
}
internal fun KSType.isFlow() = declaration.qualifiedName?.asString() == flowClassName

internal fun KSType.isSerializable() = isBuiltinSerializableType() || isUserDefinedSerializable()


private val serializableClassName = Serializable::class.simpleName
private val flowClassName = Flow::class.qualifiedName

private fun KSType.isUserDefinedSerializable() =
    declaration.annotations.any { it.shortName.asString() == serializableClassName }

private fun KSType.isBuiltinSerializableType() =
    declaration.qualifiedName?.asString() in builtinSerializableTypes

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
    List::class,
    Pair::class,
    Triple::class,
    Map.Entry::class,
    Map::class,
).map { it.qualifiedName!! }.toHashSet()

private fun KSType.serializerString(): FormattedString {
    val string = if (arguments.isNotEmpty()) {
        when (this.declaration.qualifiedName!!.asString()) {
            List::class.qualifiedName -> specialSerializer("ListSerializer", 1)
            Set::class.qualifiedName -> specialSerializer("SetSerializer", 1)
            Map::class.qualifiedName -> specialSerializer("MapSerializer", 2)
            Pair::class.qualifiedName -> specialSerializer("PairSerializer", 2)
            Map.Entry::class.qualifiedName -> specialSerializer("MapEntrySerializer", 2)
            Triple::class.qualifiedName -> specialSerializer("TripleSerializer", 3)
            else -> genericSerializerString()
        }
    } else {
        "%T.serializer()".formatType(this.toTypeName().copy(nullable = false))
    }
    return if (isMarkedNullable) "%FS.nullable".formatWith(string)
    else string
}

private fun KSType.genericSerializerString(): FormattedString {
    val argumentSerializers = arguments.map { it.type!!.serializerString() }
        .join(", ")
    return "%T.serializer(%FS)".formatType(this.toTypeName().rawType)
        .formatWith(argumentSerializers)
}

private fun KSType.specialSerializer(
    name: String,
    typeArgumentAmount: Int
): FormattedString {
    assert(arguments.size == typeArgumentAmount)

    return "$name(%FS)".formatWith(
        List(typeArgumentAmount) { arguments[it].type!!.serializerString() }.join(", ")
    )
}