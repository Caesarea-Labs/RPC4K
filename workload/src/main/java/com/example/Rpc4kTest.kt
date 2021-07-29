package com.example

import io.github.natanfudge.rpc4k.Api
import io.github.natanfudge.rpc4k.ProtocolDecoder
import io.github.natanfudge.rpc4k.SerializationFormat
import io.github.natanfudge.rpc4k.impl.Rpc4kGeneratedServerUtils
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonElement

@Serializable
data class PlayerId(val num: Long)

@Serializable
data class CreateLobbyResponse(val id: Long)

//TODO:
// - SSE

class SimpleProtocol {
    fun foo() {

    }
}
class SimpleProtocolDecoder(private val protocol: SimpleProtocol, private val format: SerializationFormat) : ProtocolDecoder<SimpleProtocol>{
    override fun accept(route: String, args: List<ByteArray>): ByteArray  = when(route){
        "foo" ->  Rpc4kGeneratedServerUtils.encodeResponse(format,
            Unit.serializer(), protocol.foo(
            )
        )
        else -> error("")
    }

}

//@Api
open class UserProtocol {
    open fun createLobby(createdBy: PlayerId, otherThing: String): CreateLobbyResponse {
        println("Handled createlobby! $createdBy")
        return CreateLobbyResponse(createdBy.num + otherThing.length)
    }

    open fun killSomeone(killer: Int, shit: PlayerId, bar: Unit): UInt {
        return (killer + shit.num).toUInt()
    }

    open fun someShit(x: Int) {

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
}

