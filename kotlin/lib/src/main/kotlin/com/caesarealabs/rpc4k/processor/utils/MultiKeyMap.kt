//package com.caesarealabs.rpc4k.processor.utils
//
//internal class MultiKeyMap<K, V> {
//    /**
//     * Map from key to "set key" of the key
//     */
//    private val keysToSets = mutableMapOf<K, Int>()
//    private val setsToValues = mutableMapOf<Int, List<V>>()
//    operator fun set(key: K, value: V) {
//        val existingSet = keysToSets[key]
//        if (existingSet != null) {
//            val currentValue = setsToValues[existingSet]
//            // If we want to change the value, we need to move the key to another set
//            if(value != currentValue) {
//                // Add new set to registry
//                val newSet = mutableSetOf(key)
//                keysToSets[key] = newSet
//                setsToValues
//                existingSet.remove(key)
//            }
//            existingSet.add(key)
//        }
//    }
//
//    fun putMany(value: V, vararg keys: K) {
//
//    }
//
//    fun get(key: K): V {
//
//    }
//}
//
//val x: HashSet