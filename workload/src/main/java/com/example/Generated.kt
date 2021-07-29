package com.example

//public class UserProtocolClientImpl(
//    private val client: RpcClient
//) : UserProtocol() {
//    public override fun createLobby(createdBy: PlayerId, otherThing: String): CreateLobbyResponse =
//        Rpc4KGeneratedClientUtils.send(
//            client,
//            "createLobby",
//            listOf(
//                createdBy to PlayerId.serializer(),
//                otherThing to String.serializer()
//            ),
//            CreateLobbyResponse.serializer()
//        )
//
////    override fun moreTypes(list: List<PlayerId>) {
////        Rpc4KGeneratedClientUtils.send(
////            client,
////            "moreTypes",
////            listOf(
////                list to ListSerializer()
////            ),
////            CreateLobbyResponse.serializer()
////        )
////    }
//
//    public override fun killSomeone(
//        killer: Int,
//        shit: PlayerId,
//        bar: Unit
//    ): UInt = Rpc4KGeneratedClientUtils.send(
//        client,
//        "killSomeone",
//        listOf(
//            killer to Int.serializer(),
//            shit to PlayerId.serializer(),
//            bar to Unit.serializer()
//        ),
//        UInt.serializer()
//    )
//
//    public override fun someShit(): Unit = Rpc4KGeneratedClientUtils.send(
//        client,
//        "someShit",
//        listOf(
//
//        ),
//        Unit.serializer()
//    )
//}
