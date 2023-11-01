package io.github.natanfudge.rpc4k.processor.utils

inline fun String.appendIf(condition: Boolean, toAppend: () -> String) = if (condition) this + toAppend() else this

inline fun <T, V> Iterable<T>.findDuplicate(byValue: (T) -> V): V? {
    val keys = hashSetOf<V>()
    for (item in this) {
        val key = byValue(item)
        if (key in keys) {
            return key
        }
        keys.add(key)
    }
    return null
}

inline fun <T> List<T>.appendIf(condition: Boolean, element: () -> T) = if (condition) this + element() else this