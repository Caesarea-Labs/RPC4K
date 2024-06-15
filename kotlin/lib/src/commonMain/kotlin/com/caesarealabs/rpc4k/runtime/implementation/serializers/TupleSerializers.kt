@file:OptIn(ExperimentalSerializationApi::class)
@file:Suppress("UNCHECKED_CAST", "FunctionName")

package com.caesarealabs.rpc4k.runtime.implementation.serializers

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeCollection


/**
 * Serializes a pair as an array of two elements
 */
public fun <K, V> TuplePairSerializer(keySerializer: KSerializer<K>, valueSerializer: KSerializer<V>): KSerializer<Pair<K, V>> = BuiltinTupleSerializer(
    listOf(keySerializer, valueSerializer), { listOf(it.first, it.second) }, { it[0] as K to it[1] as V }
)


/**
 * Serializes a triple as an array of three elements
 */
public fun <T1, T2, T3> TupleTripleSerializer(
    firstSerializer: KSerializer<T1>, secondSerializer: KSerializer<T2>, thirdSerializer: KSerializer<T3>
): KSerializer<Triple<T1, T2, T3>> = BuiltinTupleSerializer(
    listOf(firstSerializer, secondSerializer, thirdSerializer),
    { listOf(it.first, it.second, it.third) },
    { Triple(it[0] as T1, it[1] as T2, it[2] as T3) }
)


/**
 * Serializes a map entry the same way as a pair - array of two elements
 */
public fun <K, V> TupleMapEntrySerializer(keySerializer: KSerializer<K>, valueSerializer: KSerializer<V>): KSerializer<Map.Entry<K, V>> = BuiltinTupleSerializer(
    listOf(keySerializer, valueSerializer), { listOf(it.key, it.value) }, { MapEntryImpl(it[0] as K, it[1] as V) }
)

private class MapEntryImpl<K, V>(override val key: K, override val value: V) : Map.Entry<K, V> {
    override fun equals(other: Any?): Boolean {
        return other is Map.Entry<*, *> && other.key == key && other.value == value
    }

    override fun toString(): String {
        return "$key=$value"
    }

    override fun hashCode(): Int {
        var result = key?.hashCode() ?: 0
        result = 31 * result + (value?.hashCode() ?: 0)
        return result
    }
}


/**
 * Serializes tuples as an array
 */
private class BuiltinTupleSerializer<T>(elementSerializers: List<KSerializer<*>>, private val toList: (T) -> List<*>, private val fromList: (List<*>) -> T) :
    KSerializer<T> {
    private val length = elementSerializers.size
    private val delegate = TupleSerializer(elementSerializers)
    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun deserialize(decoder: Decoder): T {
        val list = delegate.deserialize(decoder)
        require(list.size == length) {
            "Expected a tuple of exactly $length elements, got ${list.size} elements"
        }
        return fromList(list)
    }

    override fun serialize(encoder: Encoder, value: T) {
        delegate.serialize(encoder, toList(value))
    }
}

/**
 * Adapted from [kotlinx.serialization.internal.CollectionLikeSerializer], [kotlinx.serialization.internal.CollectionSerializer]
 * and [kotlinx.serialization.internal.ArrayListSerializer]
 */
public class TupleSerializer(private val elementSerializers: List<KSerializer<*>>) : KSerializer<List<*>> {
    override val descriptor: SerialDescriptor = TupleDescriptor(elementSerializers.map { it.descriptor })

    override fun deserialize(decoder: Decoder): List<*> {
        val builder = arrayListOf<Any?>()
        val startIndex = 0
        val compositeDecoder = decoder.beginStructure(descriptor)
        if (compositeDecoder.decodeSequentially()) {
            readAll(compositeDecoder, builder, readSize(compositeDecoder, builder))
        } else {
            while (true) {
                val index = compositeDecoder.decodeElementIndex(descriptor)
                if (index == CompositeDecoder.DECODE_DONE) break
                readElement(compositeDecoder, startIndex + index, builder)
            }
        }
        compositeDecoder.endStructure(descriptor)
        return builder
    }

    private fun readAll(decoder: CompositeDecoder, list: ArrayList<Any?>, size: Int) {
        require(size >= 0) { "Size must be known in advance when using READ_ALL" }
        for (index in 0 until size) {
            readElement(decoder, index, list)
        }
    }

    private fun readElement(decoder: CompositeDecoder, index: Int, builder: ArrayList<Any?>) {
        builder.add(index, decoder.decodeSerializableElement(descriptor, index, elementSerializers[index]))
    }


    override fun serialize(encoder: Encoder, value: List<*>) {
        val size = value.size
        encoder.encodeCollection(descriptor, size) {
            value.forEachIndexed { i, item ->
                encodeSerializableElement(descriptor, i, elementSerializers[i] as KSerializer<Any?>, item)
            }
        }
    }

    private fun readSize(decoder: CompositeDecoder, builder: ArrayList<Any?>): Int {
        val size = decoder.decodeCollectionSize(descriptor)
        builder.ensureCapacity(size)
        return size
    }

}


/**
 * Adapted from [kotlinx.serialization.internal.ListLikeDescriptor]
 */
internal class TupleDescriptor(val elementDescriptors: List<SerialDescriptor>) : SerialDescriptor {
    override val kind: SerialKind get() = StructureKind.LIST
    override val elementsCount: Int = elementDescriptors.size
    override val serialName = "Tuple"

    override fun getElementName(index: Int): String = index.toString()
    override fun getElementIndex(name: String): Int =
        name.toIntOrNull() ?: throw IllegalArgumentException("$name is not a valid list index")

    override fun isElementOptional(index: Int): Boolean {
        validateIndex(index)
        return false
    }

    override fun getElementAnnotations(index: Int): List<Annotation> {
        validateIndex(index)
        return emptyList()
    }

    override fun getElementDescriptor(index: Int): SerialDescriptor {
        validateIndex(index)
        return elementDescriptors[index]
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TupleDescriptor) return false
        if (elementDescriptors == other.elementDescriptors && serialName == other.serialName) return true
        return false
    }

    override fun hashCode(): Int {
        return elementDescriptors.hashCode() * 31 + serialName.hashCode()
    }

    override fun toString(): String = "$serialName(${elementDescriptors.joinToString()})"

    private fun validateIndex(index: Int) = require(index in elementDescriptors.indices) {
        "Illegal index $index, $serialName expects only indices in the range ${elementDescriptors.indices}"
    }
}