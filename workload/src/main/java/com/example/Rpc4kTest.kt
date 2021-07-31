package com.example

import io.github.natanfudge.rpc4k.Api
import io.github.natanfudge.rpc4k.ProtocolDecoder
import io.github.natanfudge.rpc4k.RpcClient
import io.github.natanfudge.rpc4k.SerializationFormat
import io.github.natanfudge.rpc4k.impl.Rpc4KGeneratedClientUtils
import io.github.natanfudge.rpc4k.impl.Rpc4kGeneratedServerUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
data class PlayerId(val num: Long)

@Serializable
data class CreateLobbyResponse(val id: Long)

//TODO:
// - SSE

open class SimpleProtocol {
    open fun foo(thing: Int): Flow<Int> {
        return flowOf(1 + thing, 2, 3)
    }
    open fun bar(thing: Int): Int {
        return   thing + 1
    }
}

class SimpleProtocolDecoder(private val protocol: SimpleProtocol, private val format: SerializationFormat) :
    ProtocolDecoder<SimpleProtocol> {
    override fun accept(route: String, args: List<ByteArray>): Any = when (route) {
        "bar" -> Rpc4kGeneratedServerUtils.encodeResponse(
            this.format, Int.serializer(), this.protocol.bar(
                Rpc4kGeneratedServerUtils.decodeParameter(this.format, Int.serializer(), args[0]),
            )
        )
        "foo" -> Rpc4kGeneratedServerUtils.encodeFlowResponse(
            this.format, Int.serializer(), this.protocol.foo(
                Rpc4kGeneratedServerUtils.decodeParameter(this.format, Int.serializer(), args[0]),
            )
        )
        else -> error("")
    }

}

public class SimpleProtocolClientImpl(
    private val client: RpcClient
) : SimpleProtocol() {
    public override fun bar(thing: Int): Int  =
        Rpc4KGeneratedClientUtils.send(
            this.client,
            "bar",
            listOf(
                thing to Int.serializer(),
            ),
            Int.serializer()
        )

    public override fun foo(thing: Int): Flow<Int>  =
        Rpc4KGeneratedClientUtils.sendFlow(
            this.client,
            "foo",
            listOf(
                thing to Int.serializer(),
            ),
            Int.serializer()
        )
}



@Api
open class UserProtocol {
    open fun createLobby(createdBy: PlayerId, otherThing: String): CreateLobbyResponse {
        println("Handled createlobby! $createdBy")
        return CreateLobbyResponse(createdBy.num + otherThing.length)
    }

    open fun killSomeone(killer: Int, shit: PlayerId, bar: Unit): UInt {
        return (killer + shit.num).toUInt()
    }

    open fun someShit(x: Int, y: Int): String {
        println("asdf")
        println("asdf")
        println("asdf")
        println("asdf")
        println("asdf")
        println("asdf")
        return "asdf"
    }

    open fun moreTypes(
        list: List<PlayerId>,
        double: List<Set<String>>,
        pair: Pair<Int, Long>,
        triple: Triple<Unit, PlayerId, String>,
        entry: Map.Entry<Int, Int>
    ): Map<Long, Map<Set<List<PlayerId>>, Double>> {
        return mapOf()
    }

    open fun test(
        pair: Pair<Int, Long>,
    ): Pair<Triple<Int, Int, String>, Double> {
        return Triple(1, 2, "3") to 4.0
    }

    open fun nullable(mayNull: List<PlayerId>?, mayNull2: List<PlayerId?>) {

    }

    open fun flowTest(thing: Int) : Flow<List<PlayerId>?>{
        return flowOf(listOf(PlayerId(thing.toLong())))
    }
}

