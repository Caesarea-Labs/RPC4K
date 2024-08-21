package com.caesarealabs.rpc4k.runtime.platform

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Creates a [MutableMap] that is safe to edit in multiple threads at the same time
 */
public actual fun <K, V> ConcurrentMutableMap(): MutableMap<K,V> = ConcurrentHashMap<K,V>()

/**
 * Creates a [MutableCollection] that is safe to edit in multiple threads at the same time.
 * This is intended to be used as a queue because the most performant implementation is a queue. (In regards to time complexity)
 */
public actual fun <T> ConcurrentQueue(): MutableCollection<T> = ConcurrentLinkedQueue()