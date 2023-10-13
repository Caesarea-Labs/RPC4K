@file:OptIn(ExperimentalSerializationApi::class)
@file:Suppress("UNCHECKED_CAST")

package io.github.natanfudge.rpc4k.runtime.implementation

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeCollection
import kotlinx.serialization.json.Json
import kotlin.reflect.jvm.internal.ReflectProperties.Val


/**
 * Adapted from [kotlinx.serialization.internal.CollectionLikeSerializer], [kotlinx.serialization.internal.CollectionSerializer]
 * and [kotlinx.serialization.internal.ArrayListSerializer]
 */
class HeterogeneousListSerializer(private val elementSerializers: List<KSerializer<*>>): KSerializer<List<*>> {
    override val descriptor: SerialDescriptor = HeterogeneousListDescriptor(elementSerializers.map { it.descriptor })

    override fun deserialize(decoder: Decoder): List<*> {
        val builder = arrayListOf<Any?>()
        val startIndex = 0
        val compositeDecoder = decoder.beginStructure(descriptor)
        if (compositeDecoder.decodeSequentially()) {
            readAll(compositeDecoder,  builder, readSize(compositeDecoder, builder))
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

    private fun readAll(decoder: CompositeDecoder,list: ArrayList<Any?>,  size: Int) {
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
internal class HeterogeneousListDescriptor(val elementDescriptors: List<SerialDescriptor>) : SerialDescriptor {
    override val kind: SerialKind get() = StructureKind.LIST
    override val elementsCount: Int = elementDescriptors.size
    override val serialName = "HeterogeneousList"

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
        if (other !is HeterogeneousListDescriptor) return false
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