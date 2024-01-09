package com.caesarealabs.rpc4k.processor.utils

internal inline fun String.appendIf(condition: Boolean, toAppend: () -> String) = if (condition) this + toAppend() else this

internal inline fun <T, V> Iterable<T>.findDuplicate(byValue: (T) -> V): V? {
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

internal inline fun <T> List<T>.appendIf(condition: Boolean, element: () -> T) = if (condition) this + element() else this

/**
 * If any item is null, returns null. Otherwise, returns a non-null list.
 */
internal fun <T> List<T?>.allOrNothing(): List<T>? {
    @Suppress("UNCHECKED_CAST")
    if (any { it == null }) return null
    else return this as List<T>
}