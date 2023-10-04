package io.github.natanfudge.rpc4k
import io.github.natanfudge.rpc4k.runtime.impl.encodeAndJoin
import io.github.natanfudge.rpc4k.runtime.impl.splitJoinedAndDecode
import java.nio.charset.Charset
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class EncodingTest {


    private fun assertByteArrayListEquals(list1: List<ByteArray>, list2: List<ByteArray>) {
        assertEquals(list1.size, list2.size)
        list1.zip(list2).forEach { (array1, array2) -> assertContentEquals(array1, array2) }
    }

    @Test
    fun `Empty List is combined and split properly`() {
        assertByteArrayListEquals(listOf(), listOf<ByteArray>().encodeAndJoin().splitJoinedAndDecode())
    }


    @Test
    fun `Single empty element is combined and split properly`() {
        val arrays = listOf(byteArrayOf())
        val encoded = arrays.encodeAndJoin()
        val decoded = encoded.splitJoinedAndDecode()
        assertByteArrayListEquals(arrays, decoded)
    }

    @Test
    fun `Various cases are combined and split properly`() {
        val testData = listOf(
            listOf(byteArrayOf(0x00)),
            listOf(byteArrayOf(0x01)),
            listOf(byteArrayOf(0x02)),
            listOf(byteArrayOf(0x00, 0x01)),
            listOf(byteArrayOf(0x00, 0x02)),
            listOf(byteArrayOf(0x02, 0x00)),
            listOf(byteArrayOf(0x02, 0x01)),
            listOf(byteArrayOf(), byteArrayOf()),
            listOf(byteArrayOf(0x00), byteArrayOf()),
            listOf(byteArrayOf(), byteArrayOf(0x01)),
            listOf(byteArrayOf(0x02), byteArrayOf()),
            listOf(byteArrayOf(), byteArrayOf(0x03)),
            listOf(byteArrayOf(0x01, 0x02), byteArrayOf(0x03)),
            listOf(byteArrayOf(0x01, 0x00), byteArrayOf(0x03)),
            listOf(byteArrayOf(0x01, 0x00), byteArrayOf()),
            listOf(byteArrayOf(0x01, 0x03), byteArrayOf()),
        )
        for (arrays in testData) {
            val encoded = arrays.encodeAndJoin()
            val decoded = encoded.splitJoinedAndDecode()
            assertByteArrayListEquals(arrays, decoded)
        }
    }

    @Test
    fun `Simple Strings are combined and split properly`() {
        val strings = listOf(
            "a"
        )
        val asByteArray = strings.map { it.toByteArray() }
        val joined = asByteArray.encodeAndJoin()
        val split = joined.splitJoinedAndDecode()
        assertByteArrayListEquals(asByteArray, split)
        val backToString = split.map { it.toString(Charset.defaultCharset()) }
        assertContentEquals(strings, backToString)
    }

    @Test
    fun `Strings are combined and split properly`() {
        val strings = listOf(
            """
                        val array = byteArrayOf(0x00, 0x01, 0x00, 0x03, 0x01, 0x04, 0x00)
                        val encoded = array.encodeToJoinable()
                        assertEquals(12, encoded.size)
                        val decoded = encoded.decodeJoinable()
                        assertContentEquals(array, decoded)
            """,
            """
                Of(0x00, 0x01, 0x00, 0x03, 0x01, 0x04, 0x00)
        val encoded = array.encodeToJoinable()
        assertEquals(12, encoded.size)
            """,
            "ntEqual",
            "",
            """codeJoinable()
        assertContentEquals(array, decoded)
    }

    @Test
    fun `Strings are combined and """
        )
        val joined = strings.map { it.toByteArray() }.encodeAndJoin()
        val split = joined.splitJoinedAndDecode()
        val backToString = split.map { it.toString(Charset.defaultCharset()) }
        assertContentEquals(strings, backToString)
    }
}