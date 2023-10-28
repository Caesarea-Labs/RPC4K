@file:OptIn(ExperimentalUnsignedTypes::class)
@file:Suppress("EqualsOrHashCode")

package com.example

import com.example.EnumArgs.Option1
import com.example.EnumArgs.Option5
import io.github.natanfudge.rpc4k.runtime.api.Api
import io.github.natanfudge.rpc4k.runtime.api.serverRequirement
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZonedDateTime
import java.util.Locale
import java.util.UUID


//TODO: bug: UUID was allowed to be referenced somehow even though it is not serializable (probably because it had @Serializable(with) attached,
// need to see what i can do with that)
//TODO: add UUIDs to the format? I think i128 would work.

//@Serializable data class LyingSerializable(@Contextual val locale: Locale)


@Serializable
data class PlayerId(val num: Long)

@Serializable
@JvmInline
value class InlineId(val num: Long)

@Serializable
data class CreateLobbyResponse(val id: Long)

@Api(true)
open class SimpleProtocol {
    companion object;

    open suspend fun bar(thing: Int): Int {
        return thing + 1
    }
}

typealias AliasTest = CreateLobbyResponse

@Api(true)
open class AllEncompassingService(val value: Int  = 1) {
    companion object {
        fun distraction1() {}
        val distraction2: String = ""

    }

    val distraction3 = 2
    private fun distraction4() {}

    class Distraction5


    open suspend fun createLobby(createdBy: PlayerId, otherThing: String): CreateLobbyResponse {
        println("Handled createlobby! $createdBy")
        return CreateLobbyResponse(createdBy.num + otherThing.length)
    }

    open suspend fun killSomeone(killer: Int, shit: PlayerId, bar: Unit): UInt {
        return (killer + shit.num).toUInt()
    }

    open suspend fun someShit(x: Int, y: Int): String {
        println("asdf")
        println("asdf")
        println("asdf")
        println("asdf")
        println("asdf")
        println("asdf")
        return "asdf"
    }

    open suspend fun moreTypes(
        list: List<PlayerId>,
        double: List<Set<String>>,
        pair: Pair<Int, Long>,
        triple: Triple<Unit, PlayerId, String>,
        entry: Map.Entry<Int, Int>
    ): Map<Long, Map<InlineId, Double>> {
        return mapOf()
    }

    open suspend fun test(
        pair: Pair<Int, Long>,
    ): Pair<Triple<Int, Int, String>, Double> {
        return Triple(1, 2, "3") to 4.0
    }


    open suspend fun nullable(mayNull: List<PlayerId>?, mayNull2: List<PlayerId?>) {

    }

    @Serializable
    enum class HeavyNullableTestMode {
        EntirelyNull,
        NullList,
        NullString
    }

    open suspend fun heavyNullable(mode: HeavyNullableTestMode): GenericThing<List<String?>?, List<String>?, List<String?>>? {
        return when (mode) {
            HeavyNullableTestMode.EntirelyNull -> null
            HeavyNullableTestMode.NullList -> GenericThing(null, null, listOf())
            HeavyNullableTestMode.NullString -> GenericThing(listOf(null, "test"), null, listOf())
        }
    }

//    open suspend fun flowTest(thing: Int): Flow<List<PlayerId>?> {
//        return flowOf(listOf(PlayerId(thing.toLong())))
//    }
//
//    open suspend fun sharedFlowTest(thing: Int): Flow<List<PlayerId>?> {
//        val flow = flowOf(listOf(PlayerId(thing.toLong())))
//        return flow.stateIn(CoroutineScope(currentCoroutineContext()))
//    }

    open suspend fun genericTest(thing: String): GenericThing<String, Int, Long> {
        return GenericThing("", 2, 3)
    }

    open suspend fun errorTest() {
        throw Exception("")
    }

    open suspend fun requirementTest() {
        // Literally 1984
        serverRequirement(4 == 5) { "1984" }
    }

    open suspend fun noArgTest() {
        println("Halo")
    }

    open suspend fun requirementFail(value: Int) {
        serverRequirement(value == 2) { "Value must be 2" }
    }


    open suspend fun withNullsTest(withNulls: WithNulls<String>): WithNulls<Int> {
        return WithNulls(x = listOf(1, null), y = withNulls.y)
    }

    open suspend fun enumArgsTest(enumArgs: EnumArgs): EnumArgs {
        return when (enumArgs) {
            Option1 -> Option5
            Option5 -> Option1
        }
    }

    open suspend fun directObjectTest(obj: TheObject): TheObject {
        return obj
    }


    open suspend fun polymorphicTest(obj: PolymorphicThing): PolymorphicThing {
        return obj
    }

    open suspend fun directPolymorphicAccess(obj: PolymorphicThing.Option1): PolymorphicThing.Option1 {
        return obj
    }

    open suspend fun polymorphicClassTest(obj: PolymorphicClass): PolymorphicClass {
        return obj
    }

    open suspend fun everyBuiltinType(obj: EveryBuiltinType): EveryBuiltinType {
        return obj
    }

    /**
     *
     */
    open suspend fun everyBuiltinTypeParams(
        a: Boolean,
        b: Byte,
        c: Short,
        d: Int,
        e: Long,
        f: Char,
        g: String,
        h: ByteArray,
        i: ShortArray,
        j: IntArray,
        k: LongArray,
        l: CharArray,
        m: List<Int>,
        n: Map<Int, Int>,
        o: Set<Int>,
        p: Pair<Int, Int>,
        q: Triple<Int, Int, Int>,
        r: Unit,
        s: Array<Int>,
        t: UByteArray,
        u: UShortArray,
        v: UIntArray,
        w: ULongArray,
        x: UByte,
        y: UShort,
        z: UInt,
        a2: ULong,
        b2: Float,
        c2: Double,
        d2: Map.Entry<Int, Int>,
        e2: Instant,
        f2: ZonedDateTime,
        g2: UUID
    ): Triple<Int, Int, Int> {
        return q
    }

    open suspend fun someBuiltinTypes(types: SomeBuiltinTypes): SomeBuiltinTypes {
        return types
    }

    open suspend fun returningDataEnum(args: EnumArgs): EnumArgs {
        return args
    }

    open suspend fun aliasTest(aliasTest: AliasTest): AliasTest {
        return aliasTest
    }

    open suspend fun aliasReferenceTest(ref: TypeAliasReference): TypeAliasReference {
        return ref
    }

    open suspend fun genericsReferenceTest(ref: SomeGeneric): SomeGeneric {
        return ref
    }

}

@Serializable
data class OnlyReferencedThroughGenerics(val x: Int)
typealias SomeGeneric = List<OnlyReferencedThroughGenerics>


@Serializable
data class OnlyTypeAliased(val str: String)

typealias TypeAliasReference = OnlyTypeAliased

@Serializable
data class SomeBuiltinTypes(@Contextual val p: Pair<Int, Int>)


@Serializable
class EveryBuiltinType(
    val a: Boolean,
    val b: Byte,
    val c: Short,
    val d: Int,
    val e: Long,
    val f: Char,
    val g: String,
    val h: ByteArray,
    val i: ShortArray,
    val j: IntArray,
    val k: LongArray,
    val l: CharArray,
    val m: List<Int>,
    val n: Map<Int, Int>,
    val o: Set<Int>,
    @Contextual val p: Pair<Int, Int>,
    @Contextual val q: Triple<Int, Int, Int>,
    @Contextual val r: Unit,
    val s: Array<Int>,
    val t: UByteArray,
    val u: UShortArray,
    val v: UIntArray,
    val w: ULongArray,
    val x: UByte,
    val y: UShort,
    val z: UInt,
    val a2: ULong,
    val b2: Float,
    val c2: Double,
    @Contextual val d2: Map.Entry<Int, Int>,
    @Contextual val e2: Instant,
    @Contextual val f2: ZonedDateTime,
    @Contextual val g2: UUID
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EveryBuiltinType

        if (a != other.a) return false
        if (b != other.b) return false
        if (c != other.c) return false
        if (d != other.d) return false
        if (e != other.e) return false
        if (f != other.f) return false
        if (g != other.g) return false
        if (!h.contentEquals(other.h)) return false
        if (!i.contentEquals(other.i)) return false
        if (!j.contentEquals(other.j)) return false
        if (!k.contentEquals(other.k)) return false
        if (!l.contentEquals(other.l)) return false
        if (m != other.m) return false
        if (n != other.n) return false
        if (o != other.o) return false
        if (p != other.p) return false
        if (q != other.q) return false
        if (r != other.r) return false
        if (!s.contentEquals(other.s)) return false
        if (!t.contentEquals(other.t)) return false
        if (!u.contentEquals(other.u)) return false
        if (!v.contentEquals(other.v)) return false
        if (!w.contentEquals(other.w)) return false
        if (x != other.x) return false
        if (y != other.y) return false
        if (z != other.z) return false
        if (a2 != other.a2) return false
        if (b2 != other.b2) return false
        if (c2 != other.c2) return false
        if (d2 != other.d2) return false
        if (e2 != other.e2) return false
        if (f2 != other.f2) return false

        return true
    }
}


@Serializable
enum class EnumArgs(val x: Int) {
    Option1(3),
    Option5(8)
}


@Serializable
object TheObject

@Serializable
sealed interface PolymorphicThing {
    @Serializable
    data class Option1(val x: Int) : PolymorphicThing

    @Serializable
    data object Option2 : PolymorphicThing
}

@Serializable
sealed class PolymorphicClass {
    @Serializable
    data class Option4(val x: Int) : PolymorphicClass()

    @Serializable
    data object Option3 : PolymorphicClass()
}


@Serializable
data class GenericThing<T1, T2, T3>(val x: T1, val y: T2, val z: T3, val w: List<T3> = listOf())

@Serializable
data class WithNulls<T>(val x: List<T?>, val y: String?)