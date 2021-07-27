//import kotlinx.serialization.KSerializer
//import kotlinx.serialization.Serializable
//
//open class Protocol {
//    private val implDetail = mutableListOf<String>()
//    open fun doThing(primitive: String, serializable: SomeData): SomeResponse {
//        implDetail.add(primitive)
//        return SomeResponse(serializable.x.toLong())
//    }
//}
//
//fun client() {
//    val autoClient = GeneratedProtocolClient()
//}
//
//object Server {
//    fun req(request: String): String {
//
//    }
//}
//
//fun generatedServerHandle(requestMethod: String, requestArgs: String) : Pair<String,KSerializer<*>> {
//
//}
//
//class GeneratedProtocolServer {
//    fun handle
//}
//
//class StringConsumer {
//    fun consume(str: String): String {
//        println("Sending to server... $str")
//        return Server.req(str)
//    }
//}
//
//class GeneratedProtocolClient(private val consumer: StringConsumer) : Protocol() {
//    override fun doThing(primitive: String, serializable: SomeData): SomeResponse {
//
//    }
//}
//
//@Serializable
//data class SomeResponse(val y: Long)
//
//@Serializable
//data class SomeData(val x: Int)