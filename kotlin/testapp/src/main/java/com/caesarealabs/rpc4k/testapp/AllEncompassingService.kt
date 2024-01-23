@file:OptIn(ExperimentalUnsignedTypes::class)
@file:Suppress("EqualsOrHashCode", "unused", "MayBeConstant")

package com.caesarealabs.rpc4k.testapp

import com.caesarealabs.rpc4k.generated.AllEncompassingServiceEventInvoker
import com.caesarealabs.rpc4k.runtime.api.*
import com.caesarealabs.rpc4k.testapp.EnumArgs.Option1
import com.caesarealabs.rpc4k.testapp.EnumArgs.Option5
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZonedDateTime
import java.util.*
import kotlin.time.Duration

@Serializable
data class NestedObject(val x : Int)
@Serializable
data class NestingObject(val nested: NestedObject)


@Serializable
@JvmInline
value class GenericInline<T>(val num: T)

@Serializable
data class InlineHolder2(val value: InlineId)


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
 class AllEncompassingService(val invoker: AllEncompassingServiceEventInvoker) {
    companion object {
        fun distraction1() {}
        val distraction2: String = ""
    }

    val distraction3 = 2
    private fun distraction4() {}

    class Distraction5


    fun createLobby(createdBy: PlayerId, otherThing: String): CreateLobbyResponse {
        println("Handled createLobby! $createdBy")
        return CreateLobbyResponse(createdBy.num + otherThing.length)
    }

    fun killSomeone(killer: Int, shit: PlayerId, bar: Unit): UInt {
        return (killer + shit.num).toUInt()
    }

    fun someShit(x: Int, y: Int): String {
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
        g2: UUID,
        h2: Duration
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

    open suspend fun genericInline(inline: GenericInline<Int>): GenericInline<Int> {
        return inline
    }

    open suspend fun inlineHolder(inline: InlineHolder2): InlineHolder2 {
        return inline
    }

    open suspend fun typeField(type: TypeField): TypeField {
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

    open suspend fun tree(tree: Tree<Int>): Tree<Int> {
        return tree
    }

//    open suspend fun defaultValue(obj: WithDefaultValue): WithDefaultValue {
//        return obj
//    }
//
    open suspend fun mutableThings(obj: MutableThings): MutableThings {
        return obj
    }

    open suspend fun mutableThingsAsParams(map: MutableMap<Int,Int>,  list: MutableList<Int>): MutableSet<Int> {
        return mutableSetOf(1,2,3)
    }

    open suspend fun modelsInDifferentFiles(service: ServiceSealedInterfaceInOtherFile) : ServiceSealedInterfaceInOtherFile {
        return service
    }

    open suspend fun largeHierarchy(obj: LargeHierarchy): LargeHierarchy {
        return obj
    }

    open suspend fun someMap(map: SomeMap): SomeMap {
        return map
    }

    open suspend fun nestingObject(obj: NestingObject): NestingObject {
        return obj
    }

    open suspend fun modelWithType(type: ModelWithType): ModelWithType {
        return type
    }

    @RpcEvent
    suspend fun eventTest(@Dispatch dispatchParam: Int, normalParam: String): String {
        return normalParam + dispatchParam
    }

    open suspend fun tinkerWithEvents() {
        invoker.invokeEventTest(5)
    }

    @RpcEvent
    open suspend fun complexEventTest(param1: Pair<String, ModelWithType>, @Dispatch param2: Tree<Int>, param3: TypeField): TypeField {
        return param3
    }

    open suspend fun invokeComplexEventTest() {
        invoker.invokeComplexEventTest(Tree(2, listOf()))
    }

    //TODO: 1.
    // 4. Implement dispatch/subscription gen for EventTarget

    @RpcEvent
    open suspend fun eventTargetTest(normal: String, @EventTarget target: Int,@Dispatch dispatch: Float, normalAfterTarget: Foo): String {
        return "${normal} ${target} ${dispatch} ${normalAfterTarget.x}"
    }

    open suspend fun invokeEventTargetTest(target: Int): String {
        invoker.invokeEventTargetTest(target, 1234f)
        return "12345"
    }

    @RpcEvent suspend fun noArgsTest() {

    }


//
@Serializable
data class Foo(val x: Int)




//NiceToHave: Respect @SerialName
//    open suspend fun serialName(obj: SerialNameTest): SerialNameTest {
//        return obj
//    }

}


@Serializable sealed interface ServiceSealedInterfaceInOtherFile {

}

@Serializable
data class Tree<T>(val item: T, val children: List<Tree<T>>)


@Serializable
data class TypeField(val type: String)

@Serializable
data class OnlyReferencedThroughGenerics(val x: Int)
typealias SomeGeneric = List<OnlyReferencedThroughGenerics>


@Serializable
data class OnlyTypeAliased(val str: String)

typealias TypeAliasReference = OnlyTypeAliased

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
        if (g2 != other.g2) return false
        if (h2 != other.h2) return false

        return true
    }
}
@Serializable
sealed interface InlineSealedParent
@Serializable @JvmInline value class InlineSealedChild(val value: Int): InlineSealedParent


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
sealed interface PolymorphicThing: LargeHierarchy {
    @Serializable
    data class Option1(val x: Int) : PolymorphicThing

    @Serializable
    data object Option2 : PolymorphicThing
}

@Serializable
sealed class PolymorphicClass: LargeHierarchy {
    @Serializable
    data class Option4(val x: Int) : PolymorphicClass()

    @Serializable
    data object Option3 : PolymorphicClass()
}

@Serializable
data class ModelWithType(val type: String, val other: Int)


@Serializable data class MutableThings(val map: MutableMap<String, Int>, val list: MutableList<Int>, val set: MutableSet<Int>)

@Serializable
data class GenericThing<T1, T2, T3>(val x: T1, val y: T2, val z: T3, val w: List<T3> = listOf())

@Serializable
data class WithNulls<T>(val x: List<T?>, val y: String?)

//@Serializable @SerialName("ILoveJavascript") data class SerialNameTest(val x: Int)
//@Serializable
//data class WithDefaultValue(val value: Int = 2)