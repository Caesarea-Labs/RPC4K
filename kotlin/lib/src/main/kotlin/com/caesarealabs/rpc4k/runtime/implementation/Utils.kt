package com.caesarealabs.rpc4k.runtime.implementation

import java.util.concurrent.ConcurrentLinkedQueue

internal fun <K, V> MutableMap<K, ConcurrentLinkedQueue<V>>.concurrentAdd(
    key: K,
    value: V
) {
    val list = this[key]
    if (list == null) this[key] = ConcurrentLinkedQueue<V>().apply { add(value) }
    else list.add(value)
}

internal fun <K, V> MutableMap<in K, ConcurrentLinkedQueue<V>>.concurrentRemove(
    key: K,
    value: V
) {
    val list = this[key]
    if (list != null) {
        list.remove(value)
        if (list.isEmpty()) this.remove(key)
    }
}
