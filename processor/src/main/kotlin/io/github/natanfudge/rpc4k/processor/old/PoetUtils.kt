package io.github.natanfudge.rpc4k.processor.old

import com.squareup.kotlinpoet.*
import kotlin.reflect.KClass

internal data class FormattedString(val string: String, val formatArguments: List<TypeName>) {
    companion object {
        const val FormatStringSign = "%FS"
    }

    operator fun plus(other: FormattedString) =
        FormattedString(this.string + other.string, formatArguments + other.formatArguments)

    internal fun formatWith(vararg formattedStrings: FormattedString): FormattedString =
        formatWith(formattedStrings.toList())

    // Properly puts the format arguments in the correct position in accordance with the position of the %FS in the string
    internal fun formatWith(formattedStrings: List<FormattedString>): FormattedString {
        val types = mutableListOf<TypeName>()
        var currentArgPosition = 0
        val formatStringPositions = mutableListOf<Int>()
        for (i in string.indices) {
            val possibleTypeSign = string.substringOrUntilEnd(i, i + 2)
            if (possibleTypeSign == "%T") types.add(formatArguments[currentArgPosition++])

            val possibleFormatStringSign = string.substringOrUntilEnd(i, i + FormatStringSign.length)
            if (possibleFormatStringSign == FormatStringSign) {
                // Track a %FS occurrence
                val passedFormatStrings = formatStringPositions.size
                types.addAll(formattedStrings[passedFormatStrings].formatArguments)
                formatStringPositions.add(i)
            }
        }
        require(formatStringPositions.size == formattedStrings.size)

        // %FS occurrences will be replaced with this
        val embeddedStrings = formattedStrings.mapIndexed { i, format ->
            format.string.indentAllNewLines(string.indentOfLineAt(formatStringPositions[i]))
        }
        return FormattedString(
            string.replaceRanges(
                formatStringPositions.map { it until it + FormatStringSign.length },
                embeddedStrings
            ), types
        )
    }

}

private fun String.indentOfLineAt(index: Int): String {
    // Null returned means that at index there is a newline, meaning there is no indent
    val startOfLine = getStartOfLineIndex(index) ?: return ""
    val indentAmount = getIndentAmountFrom(startOfLine, index)
    return " ".repeat(indentAmount)
}

private fun String.getIndentAmountFrom(startOfLine: Int, index: Int): Int {
    for (i in startOfLine..index) {
        if (this[i] != ' ') {
            return i - startOfLine
        }
    }
    return index - startOfLine
}

private fun String.getStartOfLineIndex(index: Int): Int? {
    if (this[index] == '\n') return null
    else {
        for (i in (0 until index).reversed()) {
            if (this[i] == '\n') return i + 1
        }
        // No new line, start of line is just the first char then
        return 0
    }
}


internal val String.format get() = FormattedString(this, listOf())
internal fun String.formatWith(vararg formattedStrings: FormattedString) = formatWith(formattedStrings.toList())
internal fun String.formatWith(formattedStrings: List<FormattedString>) = format.formatWith(formattedStrings)
internal fun String.formatType(args: List<TypeName>) = FormattedString(this, args)
internal fun String.formatType(arg: TypeName) = FormattedString(this, listOf(arg))
internal fun String.formatType(vararg arg: TypeName) = FormattedString(this, arg.toList())
internal fun Iterable<FormattedString>.join(separator: String) = FormattedString(
    joinToString(separator) { it.string }, flatMap { it.formatArguments }
)

//internal fun TypeName.toRaw(): TypeName = if (this is ParameterizedTypeName) { rawType } else this

internal fun FunSpec.Builder.addStatement(formattedString: FormattedString) {
    addStatement(formattedString.string, *formattedString.formatArguments.toTypedArray())
}

internal fun FileSpec.Builder.addClass(name: String, builder: TypeSpec.Builder.() -> Unit) =
    addType(TypeSpec.classBuilder(name).apply(builder).build())

internal fun TypeSpec.Builder.addFunction(name: String, builder: FunSpec.Builder.() -> Unit) =
    addFunction(FunSpec.builder(name).apply(builder).build())

class PrimaryConstructorBuilder {
    private data class ConstructorProperty(val name: String, val type: TypeName, val modifiers: List<KModifier>)

    private val properties = mutableListOf<ConstructorProperty>()

    fun constructorProperty(name: String, type: TypeName, vararg modifiers: KModifier) {
        properties.add(ConstructorProperty(name, type, modifiers.toList()))
    }

    fun internalAddToType(builder: TypeSpec.Builder) {
        builder.primaryConstructor(
            FunSpec.constructorBuilder()
                .apply {
                    for (property in properties) {
                        addParameter(property.name, property.type)
                    }
                }
                .build()
        )

        for (property in properties) {
            builder.addProperty(
                PropertySpec.builder(property.name, property.type)
                    .initializer(property.name)
                    .addModifiers(*property.modifiers.toTypedArray())
                    .build()
            )
        }

    }
}

fun TypeSpec.Builder.primaryConstructor(builder: PrimaryConstructorBuilder.() -> Unit) {
    PrimaryConstructorBuilder().apply(builder).internalAddToType(this)
}

fun FileSpec.Builder.importClass(clazz: KClass<*>)  {
    val name = clazz.qualifiedName!!
    addImport(name.substringBeforeLast("."), name.substringAfterLast("."))
}

