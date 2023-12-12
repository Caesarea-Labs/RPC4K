@file:Suppress("FunctionName", "UNCHECKED_CAST")

package com.caesarealabs.rpc4k.runtime.implementation.serializers

// The immutable serializers can handle the mutable objects just fine

//fun <K, V> MutableMapSerializer(keySerializer: KSerializer<K>, valueSerializer: KSerializer<V>): KSerializer<MutableMap<K, V>> =
//    MapSerializer(keySerializer, valueSerializer) as KSerializer<MutableMap<K, V>>
//
//fun <T> MutableListSerializer(elementSerializer: KSerializer<T>): KSerializer<MutableList<T>> =
//    ListSerializer(elementSerializer) as KSerializer<MutableList<T>>
//
//fun <T> MutableSetSerializer(elementSerializer: KSerializer<T>): KSerializer<MutableSet<T>> =
//    SetSerializer(elementSerializer) as KSerializer<MutableSet<T>>