package com.example

import io.github.natanfudge.rpc4k.runtime.api.Api
import io.github.natanfudge.rpc4k.runtime.api.old.utils.DecoderContext
import io.github.natanfudge.rpc4k.runtime.api.old.utils.Rpc4KGeneratedClientUtils
import io.github.natanfudge.rpc4k.runtime.api.old.utils.Rpc4kGeneratedServerUtils
import io.github.natanfudge.rpc4k.runtime.api.old.utils.RpcClientComponents
import io.github.natanfudge.rpc4k.runtime.api.old.server.ProtocolDecoder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
data class PlayerId(val num: Long)

@Serializable
data class CreateLobbyResponse(val id: Long)

open class SimpleProtocol {
    open suspend fun foo(thing: Int): Flow<Int> {
        return flowOf(1 + thing, 2, 3)
    }

    open suspend fun bar(thing: Int): Int {
        return thing + 1
    }
}

class SimpleProtocolDecoder(private val protocol: SimpleProtocol, private val context: DecoderContext) :
    ProtocolDecoder<SimpleProtocol> {
    override suspend fun accept(route: String, args: List<ByteArray>): Any {
        fun <T> p(serializer: KSerializer<T>, index: Int) =
            Rpc4kGeneratedServerUtils.decodeParameter(context, serializer, args, index)

        fun <T> r(serializer: KSerializer<T>, value: T) =
            Rpc4kGeneratedServerUtils.encodeResponse(context.format, serializer, value)

        fun <T> r(serializer: KSerializer<T>, value: Flow<T>) =
            Rpc4kGeneratedServerUtils.encodeFlowResponse(context.format, serializer, value)

        return when (route) {
            "bar" -> r(
                Int.serializer(),
                this.protocol.bar(
                    p(Int.serializer(), 0)
                )
            )
            "foo" -> r(
                Int.serializer(), this.protocol.foo(
                    p(Int.serializer(), 0),
                )
            )
            else -> error("")
        }
    }

}


public class SimpleProtocolClientImpl(
    private val client: RpcClientComponents
) : SimpleProtocol() {
    public override suspend fun bar(thing: Int): Int =
        Rpc4KGeneratedClientUtils.send(
            this.client,
            "bar",
            Int.serializer(),
            thing to Int.serializer(),
        )

    public override suspend fun foo(thing: Int): Flow<Int> =
        Rpc4KGeneratedClientUtils.sendFlow(
            this.client,
            "foo",
            Int.serializer(),
            thing to Int.serializer(),
        )
}


@Api
open class UserProtocol {

    companion object {
        fun distraction1(){}
        val distraction2 : String = ""
    }

    val distraction3 = 2
    private fun distraction4(){}

    class Distraction5

//    open suspend fun starTest( x: List<*>) {
//
//    }

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
    ): Map<Long, Map<Set<List<PlayerId>>, Double>> {
        return mapOf()
    }

    open suspend fun test(
        pair: Pair<Int, Long>,
    ): Pair<Triple<Int, Int, String>, Double> {
        return Triple(1, 2, "3") to 4.0
    }

    open suspend fun nullable(mayNull: List<PlayerId>?, mayNull2: List<PlayerId?>) {

    }

//    open suspend fun flowTest(thing: Int): Flow<List<PlayerId>?> {
//        return flowOf(listOf(PlayerId(thing.toLong())))
//    }
//
//    open suspend fun sharedFlowTest(thing: Int): Flow<List<PlayerId>?> {
//        val flow = flowOf(listOf(PlayerId(thing.toLong())))
//        return flow.stateIn(CoroutineScope(currentCoroutineContext()))
//    }

    open suspend fun genericTest(thing: String) : GenericThing<String,Int,Long> {
        return GenericThing("", 2, 3)
    }
    
    open suspend fun errorTest() {
        throw Exception("")
    }
    fun noArgTest() {
        println("Halo")
    }

    open suspend fun requirementFail(value: Int) {
        require(value == 2){"Value must be 2"}
    }
}


@Serializable
class GenericThing<T1,T2,T3>(val x: T1, val y: T2, val z: T3)