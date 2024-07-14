@file:Suppress("FunctionName")

package com.caesarealabs.rpc4k.runtime.platform

/**
 * Creates a [MutableMap] that is safe to edit in multiple threads at the same time
 */
public expect fun <K,V> ConcurrentMutableMap(): MutableMap<K, V>
/**
 * Creates a [MutableCollection] that is safe to edit in multiple threads at the same time.
 * This is intended to be used as a queue because the most performant implementation is a queue. (In regards to time complexity)
 */
public expect fun <T> ConcurrentQueue(): MutableCollection<T>