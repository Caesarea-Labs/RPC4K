package io.github.natanfudge.rpc4k

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeName

internal data class FormattedString(val string: String, val formatArguments: List<TypeName>) {
    companion object {
        const val FormatChars = "%FS"
    }

    fun mapString(map: (String) -> String) = copy(string = map(string))
    fun appendArg(arg: TypeName) = copy(formatArguments = formatArguments + arg)
    fun appendArgs(args: List<TypeName>) = copy(formatArguments = formatArguments + args)
    fun prependArg(arg: TypeName) = copy(formatArguments = listOf(arg) + formatArguments)
    fun prependArgs(args: List<TypeName>) = copy(formatArguments = args + formatArguments)

    operator fun plus(other: FormattedString) =
        FormattedString(this.string + other.string, formatArguments + other.formatArguments)

    // Properly puts the format arguments in the correct position in accordance with the position of the %FS in the string
    internal fun formatWith(formattedString: FormattedString): FormattedString {
        val types = mutableListOf<TypeName>()
        var currentArgPosition = 0
        var formatStringPosition: Int? = null
        for (i in string.indices) {
            val typeFormat = string.substringOrNull(i, i + 2)
            if (typeFormat == "%T") types.add(formatArguments[currentArgPosition++])
            val formatStringFormat = string.substringOrNull(i, i + 3)
            if (formatStringFormat == FormatChars) {
                require(formatStringPosition == null)
                formatStringPosition = i
                types.addAll(formattedString.formatArguments)
            }
        }
        requireNotNull(formatStringPosition)

        val embeddedString = formattedString.string.indentAllNewLines(string.indentOfLineAt(formatStringPosition))
        return FormattedString(string.replace(FormatChars, embeddedString), types)
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

private fun String.substringOrNull(startIndex: Int, endIndex: Int) =
    if (endIndex < length && startIndex >= 0) substring(startIndex, endIndex) else null

internal val String.format get() = FormattedString(this, listOf())
internal fun String.formatType(args: List<TypeName>) = FormattedString(this, args)
internal fun String.formatType(arg: TypeName) = FormattedString(this, listOf(arg))
internal fun String.formatType(vararg arg: TypeName) = FormattedString(this, arg.toList())
internal fun Iterable<FormattedString>.join(separator: String) = FormattedString(
    joinToString(separator) { it.string }, flatMap { it.formatArguments }
)

internal fun FunSpec.Builder.addStatement(formattedString: FormattedString) {
    addStatement(formattedString.string, *formattedString.formatArguments.toTypedArray())
}