package com.example

import io.github.natanfudge.rpc4k.Rpc4KGeneratedClientUtils
import io.github.natanfudge.rpc4k.Rpc4kGeneratedServerUtils
import io.github.natanfudge.rpc4k.Rpc4kGeneratedServerUtils.decodeParameter
import io.github.natanfudge.rpc4k.ProtocolDecoder
import io.github.natanfudge.rpc4k.RpcClient
import kotlinx.serialization.builtins.serializer

//class GeneratedUserProtocolManual(private val client: RpcClient) : UserProtocol() {
//    override fun createLobby(createdBy: PlayerId, otherThing: String) = Rpc4KGeneratedClientUtils.send(
//        client,
//        "createLobby",
//        listOf(
//            createdBy to PlayerId.serializer(),
//            otherThing to String.serializer()
//        ),
//        CreateLobbyResponse.serializer()
//    )
//}

class GeneratedProtocolDecoder(private val protocol: UserProtocol) : ProtocolDecoder<UserProtocol> {
    override fun accept(route: String, args: List<String>) = when (route) {
        "createLobby" -> Rpc4kGeneratedServerUtils.encodeResponse(
            CreateLobbyResponse.serializer(), protocol.createLobby(
                decodeParameter(PlayerId.serializer(), args[0]),
                decodeParameter(String.serializer(), args[1]),
            )
        )
        else -> Rpc4kGeneratedServerUtils.invalidRoute(route)
    }

}

fun unrelatedChange() {
    println("asdf")
}