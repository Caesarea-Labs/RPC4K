package io.github.natanfudge.rpc4k

 fun <K, V> Sequence<Pair<K, V>>.split(): Pair<Sequence<K>, Sequence<V>> {
    return map { it.first } to map { it.second }
}

 fun <K, V> List<Pair<K, V>>.split(): Pair<List<K>, List<V>> {
    return map { it.first } to map { it.second }
}

//TODO: remove
fun String.indentAllNewLines(): String {
    // Find first non-space occurrence
    val indentAmount = run {
        forEachIndexed { index, c ->  if(c != ' ') return@run index}
        return@run 0
    }
    return indentAllNewLines(" ".repeat(indentAmount))
}

fun String.indentAllNewLines(indent: String): String {
    return split("\n").joinToString("\n" + indent  )
}