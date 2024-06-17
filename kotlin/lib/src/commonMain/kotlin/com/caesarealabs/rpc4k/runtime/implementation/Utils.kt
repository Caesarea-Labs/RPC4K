package com.caesarealabs.rpc4k.runtime.implementation

import com.caesarealabs.rpc4k.runtime.platform.ConcurrentQueue

internal fun <K, V> MutableMap<K, MutableCollection<V>>.concurrentAdd(
    key: K,
    value: V
) {
    val list = this[key]
    if (list == null) this[key] = ConcurrentQueue<V>().apply { add(value) }
    else list.add(value)
}
