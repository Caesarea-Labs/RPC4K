package io.github.natanfudge.rpc4k.processor.utils

import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import io.github.natanfudge.rpc4k.processor.old.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.SetSerializer

internal fun KSType.isSerializable() = isBuiltinSerializableType() || isAnnotatedBySerializable()


private val serializableClassName = Serializable::class.qualifiedName

private fun KSType.isAnnotatedBySerializable() =
    declaration.annotations.any { it.annotationType.resolve().toClassName().canonicalName == serializableClassName }

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

private fun KSType.isBuiltinSerializableType() =
    declaration.qualifiedName?.asString() in builtinSerializableTypes

data class FormattedString(val string: String, val args: List<Any>)
private fun String.formatWith(vararg args: Any) = FormattedString(this,args.toList())

fun KSType.toSerializerString() {
    val string = if (arguments.isEmpty()) {
        "%M".formatWith(MemberName(this.toClassName(), "serializer"))
    } else {
        when (this.declaration.qualifiedName) {
            List::class.qualifiedName ->  MemberName() ListSerializer() specialSerializer("ListSerializer", 1)
            Set::class.qualifiedName -> specialSerializer("SetSerializer", 1)
            Map::class.qualifiedName -> specialSerializer("MapSerializer", 2)
            Pair::class.qualifiedName -> specialSerializer("PairSerializer", 2)
            Map.Entry::class.qualifiedName -> specialSerializer("MapEntrySerializer", 2)
            Triple::class.qualifiedName -> specialSerializer("TripleSerializer", 3)
        }
    }
}

private fun KSType.specialSerializer(
    name: String,
): FormattedString {
    //TODO: figure out if star arguments cause null type, for that we need to get the debugger to work.
    val args = arguments.map { it.type.resolve().toSerializerString() }
    "%M("
    return "$name(%FS)".formatWith(
        List(typeArgumentAmount) { arguments[it].type!!.serializerString() }.join(", ")
    )
}
//private fun KSType.serializerString(): FormattedString {
//    val string = if (arguments.isNotEmpty()) {
//        when (this.declaration.qualifiedName!!.asString()) {
//            List::class.qualifiedName -> specialSerializer("ListSerializer", 1)
//            Set::class.qualifiedName -> specialSerializer("SetSerializer", 1)
//            Map::class.qualifiedName -> specialSerializer("MapSerializer", 2)
//            Pair::class.qualifiedName -> specialSerializer("PairSerializer", 2)
//            Map.Entry::class.qualifiedName -> specialSerializer("MapEntrySerializer", 2)
//            Triple::class.qualifiedName -> specialSerializer("TripleSerializer", 3)
//            else -> genericSerializerString()
//        }
//    } else {
//        "%T.serializer()".formatType(this.toTypeName().copy(nullable = false))
//    }
//    return if (isMarkedNullable) "%FS.nullable".formatWith(string)
//    else string
//}