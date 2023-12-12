@file:OptIn(ExperimentalUnsignedTypes::class)
@file:Suppress("EqualsOrHashCode", "unused", "MayBeConstant")

package com.caesarealabs.rpc4k.testapp

import com.caesarealabs.rpc4k.testapp.EnumArgs.Option1
import com.caesarealabs.rpc4k.testapp.EnumArgs.Option5
import com.caesarealabs.rpc4k.runtime.api.Api
import com.caesarealabs.rpc4k.runtime.api.serverRequirement
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZonedDateTime
import java.util.*
import kotlin.time.Duration




//@Serializable data class LyingSerializable(@Contextual val locale: Locale)

@Serializable
@JvmInline
value class GenericInline<T>(val num: T)

@Serializable
data class InlineHolder2(val value: com.caesarealabs.rpc4k.testapp.InlineId)


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

typealias AliasTest = com.caesarealabs.rpc4k.testapp.CreateLobbyResponse

@Api(true)
open class AllEncompassingService(val value: Int = 1) {
    companion object {
        fun distraction1() {}
        val distraction2: String = ""

    }

    val distraction3 = 2
    private fun distraction4() {}

    class Distraction5


    open suspend fun createLobby(createdBy: com.caesarealabs.rpc4k.testapp.PlayerId, otherThing: String): com.caesarealabs.rpc4k.testapp.CreateLobbyResponse {
        println("Handled createLobby! $createdBy")
        return _root_ide_package_.com.caesarealabs.rpc4k.testapp.CreateLobbyResponse(createdBy.num + otherThing.length)
    }

    open suspend fun killSomeone(killer: Int, shit: com.caesarealabs.rpc4k.testapp.PlayerId, bar: Unit): UInt {
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
        list: List<com.caesarealabs.rpc4k.testapp.PlayerId>,
        double: List<Set<String>>,
        pair: Pair<Int, Long>,
        triple: Triple<Unit, com.caesarealabs.rpc4k.testapp.PlayerId, String>,
        entry: Map.Entry<Int, Int>
    ): Map<Long, Map<com.caesarealabs.rpc4k.testapp.InlineId, Double>> {
        return mapOf()
    }

    open suspend fun test(
        pair: Pair<Int, Long>,
    ): Pair<Triple<Int, Int, String>, Double> {
        return Triple(1, 2, "3") to 4.0
    }


    open suspend fun nullable(mayNull: List<com.caesarealabs.rpc4k.testapp.PlayerId>?, mayNull2: List<com.caesarealabs.rpc4k.testapp.PlayerId?>) {

    }

    @Serializable
    enum class HeavyNullableTestMode {
        EntirelyNull,
        NullList,
        NullString
    }

    open suspend fun heavyNullable(mode: com.caesarealabs.rpc4k.testapp.AllEncompassingService.HeavyNullableTestMode): com.caesarealabs.rpc4k.testapp.GenericThing<List<String?>?, List<String>?, List<String?>>? {
        return when (mode) {
            _root_ide_package_.com.caesarealabs.rpc4k.testapp.AllEncompassingService.HeavyNullableTestMode.EntirelyNull -> null
            _root_ide_package_.com.caesarealabs.rpc4k.testapp.AllEncompassingService.HeavyNullableTestMode.NullList -> _root_ide_package_.com.caesarealabs.rpc4k.testapp.GenericThing(
                null,
                null,
                listOf()
            )
            _root_ide_package_.com.caesarealabs.rpc4k.testapp.AllEncompassingService.HeavyNullableTestMode.NullString -> _root_ide_package_.com.caesarealabs.rpc4k.testapp.GenericThing(
                listOf(null, "test"),
                null,
                listOf()
            )
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

    open suspend fun genericTest(thing: String): com.caesarealabs.rpc4k.testapp.GenericThing<String, Int, Long> {
        return _root_ide_package_.com.caesarealabs.rpc4k.testapp.GenericThing("", 2, 3)
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


    open suspend fun withNullsTest(withNulls: com.caesarealabs.rpc4k.testapp.WithNulls<String>): com.caesarealabs.rpc4k.testapp.WithNulls<Int> {
        return _root_ide_package_.com.caesarealabs.rpc4k.testapp.WithNulls(x = listOf(1, null), y = withNulls.y)
    }

    open suspend fun enumArgsTest(enumArgs: com.caesarealabs.rpc4k.testapp.EnumArgs): com.caesarealabs.rpc4k.testapp.EnumArgs {
        return when (enumArgs) {
            Option1 -> Option5
            Option5 -> Option1
        }
    }

    open suspend fun directObjectTest(obj: com.caesarealabs.rpc4k.testapp.TheObject): com.caesarealabs.rpc4k.testapp.TheObject {
        return obj
    }


    open suspend fun polymorphicTest(obj: com.caesarealabs.rpc4k.testapp.PolymorphicThing): com.caesarealabs.rpc4k.testapp.PolymorphicThing {
        return obj
    }

    open suspend fun directPolymorphicAccess(obj: com.caesarealabs.rpc4k.testapp.PolymorphicThing.Option1): _root_ide_package_.com.caesarealabs.rpc4k.testapp.PolymorphicThing.Option1 {
        return obj
    }

    open suspend fun polymorphicClassTest(obj: _root_ide_package_.com.caesarealabs.rpc4k.testapp.PolymorphicClass): _root_ide_package_.com.caesarealabs.rpc4k.testapp.PolymorphicClass {
        return obj
    }

    open suspend fun everyBuiltinType(obj: _root_ide_package_.com.caesarealabs.rpc4k.testapp.EveryBuiltinType): _root_ide_package_.com.caesarealabs.rpc4k.testapp.EveryBuiltinType {
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
        g2: UUID,
        h2: Duration
    ): Triple<Int, Int, Int> {
        return q
    }

    open suspend fun someBuiltinTypes(types: _root_ide_package_.com.caesarealabs.rpc4k.testapp.SomeBuiltinTypes): _root_ide_package_.com.caesarealabs.rpc4k.testapp.SomeBuiltinTypes {
        return types
    }

    open suspend fun returningDataEnum(args: _root_ide_package_.com.caesarealabs.rpc4k.testapp.EnumArgs): _root_ide_package_.com.caesarealabs.rpc4k.testapp.EnumArgs {
        return args
    }

    open suspend fun aliasTest(aliasTest: _root_ide_package_.com.caesarealabs.rpc4k.testapp.AliasTest): _root_ide_package_.com.caesarealabs.rpc4k.testapp.AliasTest {
        return aliasTest
    }

    open suspend fun aliasReferenceTest(ref: _root_ide_package_.com.caesarealabs.rpc4k.testapp.TypeAliasReference): _root_ide_package_.com.caesarealabs.rpc4k.testapp.TypeAliasReference {
        return ref
    }

    open suspend fun genericsReferenceTest(ref: _root_ide_package_.com.caesarealabs.rpc4k.testapp.SomeGeneric): _root_ide_package_.com.caesarealabs.rpc4k.testapp.SomeGeneric {
        return ref
    }

    open suspend fun genericInline(inline: _root_ide_package_.com.caesarealabs.rpc4k.testapp.GenericInline<Int>): _root_ide_package_.com.caesarealabs.rpc4k.testapp.GenericInline<Int> {
        return inline
    }

    open suspend fun inlineHolder(inline: _root_ide_package_.com.caesarealabs.rpc4k.testapp.InlineHolder2): _root_ide_package_.com.caesarealabs.rpc4k.testapp.InlineHolder2 {
        return inline
    }

    open suspend fun typeField(type: _root_ide_package_.com.caesarealabs.rpc4k.testapp.TypeField): _root_ide_package_.com.caesarealabs.rpc4k.testapp.TypeField {
        return type
    }

    open suspend fun noArgsYesReturn(): Int {
        return 2
    }

    open suspend fun nullDate(date: Instant?): Instant?{
        return date
    }

//    This is currently bugged, see:
//https://github.com/Kotlin/kotlinx.serialization/issues/2374
//    open suspend fun inlineSealedParent(obj: InlineSealedParent): InlineSealedParent {
//        return obj
//    }
//
//    open suspend fun inlineSealedChild(obj: InlineSealedChild): InlineSealedChild {
//        return obj
//    }
//
//    open suspend fun inlineSealedChildReturnParent(obj: InlineSealedChild): InlineSealedParent {
//        return obj
//    }

    open suspend fun tree(tree: _root_ide_package_.com.caesarealabs.rpc4k.testapp.Tree<Int>): _root_ide_package_.com.caesarealabs.rpc4k.testapp.Tree<Int> {
        return tree
    }

//    open suspend fun defaultValue(obj: WithDefaultValue): WithDefaultValue {
//        return obj
//    }
//
    open suspend fun mutableThings(obj: _root_ide_package_.com.caesarealabs.rpc4k.testapp.MutableThings): _root_ide_package_.com.caesarealabs.rpc4k.testapp.MutableThings {
        return obj
    }

    open suspend fun mutableThingsAsParams(map: MutableMap<Int,Int>,  list: MutableList<Int>): MutableSet<Int> {
        return mutableSetOf(1,2,3)
    }

    open suspend fun modelsInDifferentFiles(service: _root_ide_package_.com.caesarealabs.rpc4k.testapp.ServiceSealedInterfaceInOtherFile) : _root_ide_package_.com.caesarealabs.rpc4k.testapp.ServiceSealedInterfaceInOtherFile {
        return service
    }

    open suspend fun largeHierarchy(obj: _root_ide_package_.com.caesarealabs.rpc4k.testapp.LargeHierarchy): _root_ide_package_.com.caesarealabs.rpc4k.testapp.LargeHierarchy {
        return obj
    }

    open suspend fun someMap(map: _root_ide_package_.com.caesarealabs.rpc4k.testapp.SomeMap): _root_ide_package_.com.caesarealabs.rpc4k.testapp.SomeMap {
        return map
    }

//NiceToHave: Respect @SerialName
//    open suspend fun serialName(obj: SerialNameTest): SerialNameTest {
//        return obj
//    }

}


@Serializable sealed interface ServiceSealedInterfaceInOtherFile {

}

@Serializable
data class Tree<T>(val item: T, val children: List<_root_ide_package_.com.caesarealabs.rpc4k.testapp.Tree<T>>)

@Serializable
data class TypeField(val type: String)

@Serializable
data class OnlyReferencedThroughGenerics(val x: Int)
typealias SomeGeneric = List<_root_ide_package_.com.caesarealabs.rpc4k.testapp.OnlyReferencedThroughGenerics>


@Serializable
data class OnlyTypeAliased(val str: String)

typealias TypeAliasReference = _root_ide_package_.com.caesarealabs.rpc4k.testapp.OnlyTypeAliased

@Serializable
data class SomeBuiltinTypes(@Contextual val p: Pair<Int, Int>)

@Serializable data class SomeMap(val map: Map<Int, Float>)


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
    @Contextual val g2: UUID,
    val h2: Duration
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as _root_ide_package_.com.caesarealabs.rpc4k.testapp.EveryBuiltinType

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
        if (g2 != other.g2) return false
        if (h2 != other.h2) return false

        return true
    }
}
@Serializable
sealed interface InlineSealedParent
@Serializable @JvmInline value class InlineSealedChild(val value: Int): _root_ide_package_.com.caesarealabs.rpc4k.testapp.InlineSealedParent


@Serializable
enum class EnumArgs(val x: Int) {
    Option1(3),
    Option5(8)
}


@Serializable
object TheObject

@Serializable
sealed interface LargeHierarchy

@Serializable
sealed interface PolymorphicThing: _root_ide_package_.com.caesarealabs.rpc4k.testapp.LargeHierarchy {
    @Serializable
    data class Option1(val x: Int) : _root_ide_package_.com.caesarealabs.rpc4k.testapp.PolymorphicThing

    @Serializable
    data object Option2 : _root_ide_package_.com.caesarealabs.rpc4k.testapp.PolymorphicThing
}

@Serializable
sealed class PolymorphicClass: _root_ide_package_.com.caesarealabs.rpc4k.testapp.LargeHierarchy {
    @Serializable
    data class Option4(val x: Int) : _root_ide_package_.com.caesarealabs.rpc4k.testapp.PolymorphicClass()

    @Serializable
    data object Option3 : _root_ide_package_.com.caesarealabs.rpc4k.testapp.PolymorphicClass()
}




@Serializable data class MutableThings(val map: MutableMap<String, Int>, val list: MutableList<Int>, val set: MutableSet<Int>)

@Serializable
data class GenericThing<T1, T2, T3>(val x: T1, val y: T2, val z: T3, val w: List<T3> = listOf())

@Serializable
data class WithNulls<T>(val x: List<T?>, val y: String?)

//@Serializable @SerialName("ILoveJavascript") data class SerialNameTest(val x: Int)
//@Serializable
//data class WithDefaultValue(val value: Int = 2)