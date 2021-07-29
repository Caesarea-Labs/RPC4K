package io.github.natanfudge.rpc4k

import io.github.natanfudge.rpc4k.impl.GeneratedServerImplSuffix
import io.github.natanfudge.rpc4k.impl.applyIf
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json


//fun SerialFormat.foo() {
//    this.serializersModule
//}
interface SerializationFormat {
    fun <T> encode(serializer: SerializationStrategy<T>, value: T): ByteArray
    fun <T> decode(serializer: DeserializationStrategy<T>, raw: ByteArray): T
}

private const val JoinByte = 0x01.toByte()
private const val EncodingByte = 0x00.toByte()

/**
 * We encode the bytearray so we can reliably combine it with a SplitChar and split it.
 *
 * 0x00 -> 0x00_00
 * 0x01 -> 0x00_01
 * JoinByte -> 0x01
 */
 fun ByteArray.encodeToSplitable(): ByteArray {
    // 0x00 and 0x01 are twice as long
    val newSize = size + count { it == EncodingByte || it == JoinByte }
    val newArray = ByteArray(newSize)

    var currentIndex = 0
    for (byte in this) {
        when (byte) {
            EncodingByte, JoinByte -> {
                newArray[currentIndex] = EncodingByte
                currentIndex++
            }
        }
        newArray[currentIndex] = byte
    }
    return newArray
}

 fun ByteArray.splitSplitable(): List<ByteArray> {
    if (size == 0) return listOf()
    val list = mutableListOf<ByteArray>()
    var currentArrayStart = 0
    for (i in indices) {
        val byte = this[i]
        // A new array needs to be added when a join byte is here or the end of the bytearray has been reached.
        if (byte == JoinByte || i == size - 1) {
            val endIndex = i.applyIf(byte == JoinByte) { it - 1 }
            list.add(copyOfRange(currentArrayStart, endIndex))
            currentArrayStart = i + 1
        }
    }
    return list
}

 fun ByteArray.decodeSplitable(): ByteArray {
    val originalArray = ByteArray(size - getIncreasedSizeOfSplitable())
    var encodingByte = false
    var originArrayIndex = 0
    for (i in indices) {
        val byte = this[i]
        if (encodingByte) {
            devCheck {
                byte == EncodingByte || byte == JoinByte
            }
            // 0x00_00 or 0x00_01
            originalArray[originArrayIndex++] = byte
            encodingByte = false
        } else if (byte == EncodingByte) {
            // 0x00, will wait for next byte
            encodingByte = true
        } else {
            // 0x02 and up, encoded normally
            originalArray[originArrayIndex++] = byte
        }
    }
    return originalArray
}

 fun ByteArray.getIncreasedSizeOfSplitable(): Int {
    var size = 0
    var encodingByte = false
    for (byte in this) {
        if (encodingByte) {
            size++
            encodingByte = false
        } else if (byte == EncodingByte) {
            encodingByte = true
        }
    }
    return size
}

 fun List<ByteArray>.join(separator: Byte): ByteArray {
    val separators = this.size - 1
    val newArray = ByteArray(this.sumOf { it.size } + separators)
    var currentIndex = 0
    this.forEachIndexed { i, array ->
        for (byte in array) {
            newArray[currentIndex] = byte
            currentIndex++
        }

        // Run in every loop except the last
        if (i != this.size - 1) {
            // ADd the separator
            newArray[currentIndex] = separator
            currentIndex++
        }
    }
    return newArray
}

fun List<ByteArray>.encodeAndJoin(): ByteArray = map { it.encodeToSplitable() }.join(JoinByte)
fun ByteArray.splitAndDecode(): List<ByteArray> = splitSplitable().map { it.decodeSplitable() }

//
object JsonFormat : SerializationFormat {
    private val json = Json
    private val charset = Charsets.UTF_8
    override fun <T> encode(serializer: SerializationStrategy<T>, value: T): ByteArray {
        return json.encodeToString(serializer, value).toByteArray()
    }

    override fun <T> decode(serializer: DeserializationStrategy<T>, raw: ByteArray): T {
        return json.decodeFromString(serializer, raw.toString(charset))
    }
}


interface ProtocolDecoder<P> {
    fun accept(route: String, args: List<ByteArray>): ByteArray
}

private const val devChecks = true

private inline fun devCheck(check: () -> Boolean) {
    if (devChecks && !check()) throw IllegalStateException()
}

class RpcServer<T> private constructor(
    private val decoder: ProtocolDecoder<T>,
    //TODO: configurable
    private val format: SerializationFormat = JsonFormat
) {

    companion object {
        fun <T> start(decoder: ProtocolDecoder<T>): RpcServer<T> {
            return RpcServer(decoder).apply { start() }
        }

        inline fun <reified T : Any> jvmStartWithProtocol(protocolImpl: T): RpcServer<T> {
            val protocolClass = T::class
            val decoder = Class.forName(protocolClass.qualifiedName + GeneratedServerImplSuffix)
                .getDeclaredConstructor(protocolClass.java).newInstance(protocolImpl) as ProtocolDecoder<T>
            return start(decoder)
        }
    }

    private fun start() {
        testRpcServer = this
    }

    internal fun accept(route: String, body: ByteArray): ByteArray {
        return decoder.accept(route, body.splitAndDecode())
    }
}