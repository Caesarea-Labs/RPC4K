package io.github.natanfudge.rpc4k.processor

import kotlin.math.min

internal fun String.indentAllNewLines(indent: String): String {
    return split("\n").joinToString("\n" + indent)
}

internal fun String.replaceRanges(ranges: List<IntRange>, replacements: List<String>): String {
    require(ranges.isNotEmpty())
    require(ranges.size == replacements.size)

    // For example "hello %FS here is a %FS!"  --> ["hello ", " here is a ", "!"]
    val parts = listOf(substring(0, ranges[0].first)) + ranges.mapIndexed { i, range ->
        val nextRange = ranges.getOrNull(i + 1) ?: return@mapIndexed substringOrEmpty(range.last + 1)
        substring(range.last + 1, nextRange.first)
    }

    return buildString {
        replacements.forEachIndexed { i, replacement ->
            append(parts[i] + replacement)
        }
        append(parts.last())
    }
}

internal fun String.substringOrEmpty(startIndex: Int) = if (startIndex < length) substring(startIndex) else ""
internal fun String.substringOrUntilEnd(startIndex: Int, endIndex: Int) = substring(startIndex, min(endIndex, length))

internal inline fun <T> T.applyIf(check: Boolean, apply: (T) -> T) = if (check) apply(this) else this