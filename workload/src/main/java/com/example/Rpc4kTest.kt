package com.example

import io.github.natanfudge.rpc4k.Api
import io.github.natanfudge.rpc4k.ProtocolDecoder
import io.github.natanfudge.rpc4k.RpcClient
import io.github.natanfudge.rpc4k.impl.Rpc4KGeneratedClientUtils
import io.github.natanfudge.rpc4k.impl.Rpc4kGeneratedServerUtils
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.*
import kotlinx.serialization.json.JsonElement

@Serializable
data class PlayerId(val num: Long)

@Serializable
data class CreateLobbyResponse(val id: Long)

//TODO:
// - lists
// - SSE
// - server implementation

@Api
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
    ): Pair<Triple<Int,Int,String>,Double> {
        return Triple(1,2,"3") to 4.0
    }
}

