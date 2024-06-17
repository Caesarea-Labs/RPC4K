package com.caesarealabs.rpc4k.runtime.platform

import co.touchlab.stately.collections.ConcurrentMutableList
import co.touchlab.stately.collections.ConcurrentMutableMap

/**
 * Bad implementation for now (it's slow)
 */
public actual fun <K, V> ConcurrentMutableMap(): MutableMap<K, V>  = ConcurrentMutableMap()

/**
 * Bad implementation for now (it's slow)
 */
public actual fun <T> ConcurrentQueue(): MutableCollection<T>  = ConcurrentMutableList()