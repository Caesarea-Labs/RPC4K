package com.example

import io.github.natanfudge.rpc4k.Api
import io.github.natanfudge.rpc4k.RpcClient
import io.github.natanfudge.rpc4k.RpcServer
import kotlinx.serialization.Serializable

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
        return CreateLobbyResponse(8)
    }

    open fun killSomeone(killer: Int, )
}



fun userServer() {
    RpcServer.start(GeneratedProtocolDecoder(UserProtocol()))
}