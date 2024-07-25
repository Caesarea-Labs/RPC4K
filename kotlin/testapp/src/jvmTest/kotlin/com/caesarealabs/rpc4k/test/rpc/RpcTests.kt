package com.caesarealabs.rpc4k.test.rpc

import com.caesarealabs.rpc4k.generated.AllEncompassingServiceEventInvoker
import com.caesarealabs.rpc4k.generated.AllEncompassingServiceNetworkClient
import com.caesarealabs.rpc4k.generated.SimpleProtocolEventInvoker
import com.caesarealabs.rpc4k.generated.SimpleProtocolNetworkClient
import com.caesarealabs.rpc4k.runtime.api.RpcResponseException
import com.caesarealabs.rpc4k.runtime.jvm.user.testing.ClientServerExtension
import com.caesarealabs.rpc4k.testapp.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import java.util.*
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

typealias FullService = ClientServerExtension<AllEncompassingService, AllEncompassingServiceNetworkClient, AllEncompassingServiceEventInvoker>
typealias SimpleService = ClientServerExtension<SimpleProtocol, SimpleProtocolNetworkClient, SimpleProtocolEventInvoker>

object RpcTests {
     fun testGeneratedEvents(service: FullService) = runTest {
         // Dont skip delays
         withContext(Dispatchers.Default) {
             val server = service.server
             val client = service.client
             var actualMessage: String? = null


             GlobalScope.launch {
                 client.eventTest("Test string").collectLatest {
                     actualMessage = it
                 }
             }


             delay(1000)

             server.tinkerWithEvents()

             delay(1000)

             expectThat(actualMessage).isEqualTo("Test string5")
         }

     }

     fun testUsage(service: FullService) = runTest {
         val protocol = service.client
         val response = protocol.createLobby(PlayerId(123), "alo")
         assertEquals(CreateLobbyResponse(126), response)
         val response2 = protocol.killSomeone(111, PlayerId(5), Unit)
         assertEquals(116.toUInt(), response2)
         protocol.someShit(1, 2)
         protocol.someShit(1, 2)
         val mapForEntry = mapOf(1 to 1)
         protocol.moreTypes(
             listOf(),
             listOf(),
             1 to 2,
             Triple(Unit, PlayerId(1), ""),
             mapForEntry.entries.first()
         )
         val result = protocol.test(1 to 2)
         assertEquals(Triple(1, 2, "3") to 4.0, result)
         protocol.nullable(null, listOf(null))
         println("fioi")
         println("fioi")
         protocol.someShit(1, 2)

         protocol.genericTest("")
     }

    fun testNullableTypes(service: FullService) = runTest {
        val protocol = service.client
        expectThat(protocol.heavyNullable(AllEncompassingService.HeavyNullableTestMode.EntirelyNull)).isEqualTo(null)
        expectThat(protocol.heavyNullable(AllEncompassingService.HeavyNullableTestMode.NullList)).isEqualTo(GenericThing(null, null, listOf()))
        expectThat(protocol.heavyNullable(AllEncompassingService.HeavyNullableTestMode.NullString)).isEqualTo(
            GenericThing(
                listOf(null, "test"),
                null,
                listOf()
            )
        )
    }

    fun simpleClassTest(service: SimpleService) = runTest {
        val protocol = service.client
        val response = protocol.bar(2)
        assertEquals(3, response)
    }

    fun testExceptions(service: FullService) {
        expectThrows<RpcResponseException> {
            service.client.errorTest()
        }.get { code == 500 }

        expectThrows<RpcResponseException> {
            service.client.requirementTest()
        }.get { code == 400 }
    }

    fun testExoticTypes(service: FullService) = runTest {
        val y = "Asdf"
        val protocol = service.client
        expectThat(protocol.withNullsTest(WithNulls(listOf("2", null), y = y)))
            .isEqualTo(WithNulls(listOf(1, null), y))

        expectThat(protocol.enumArgsTest(EnumArgs.Option1)).isEqualTo(EnumArgs.Option5)

        expectThat(protocol.directObjectTest(TheObject)).isEqualTo(TheObject)

        val thing: PolymorphicThing = PolymorphicThing.Option2
        expectThat(protocol.polymorphicTest(thing)).isEqualTo(thing)
        val direct: PolymorphicThing.Option1 = PolymorphicThing.Option1(2)
        expectThat(protocol.directPolymorphicAccess(direct)).isEqualTo(direct)
        val polymorphicClass = PolymorphicClass.Option4(3)
        expectThat(protocol.polymorphicClassTest(polymorphicClass)).isEqualTo(polymorphicClass)
        val everything = EveryBuiltinType(
            false, 1, 2, 3, 4, '5', "6",
            byteArrayOf(7), ShortArray(8), IntArray(9), longArrayOf(10), charArrayOf('@'),
            listOf(11), mapOf(12 to 13), setOf(14), 15 to 16, Triple(17, 18, 19), Unit,
            arrayOf(20), ubyteArrayOf(21u), ushortArrayOf(22u), uintArrayOf(23u), ULongArray(24),
            25u, 26u, 27u, 28u, 29f, 30.0, mapOf(31 to 32).entries.first(), Clock.System.now(),
            UUID.randomUUID(), 33.milliseconds

        )
        expectThat(protocol.everyBuiltinType(everything)).isEqualTo(everything)
        expectThat(
            protocol.everyBuiltinTypeParams(
                false, 1, 2, 3, 4, '5', "6",
                byteArrayOf(7), ShortArray(8), IntArray(9), longArrayOf(10), charArrayOf('@'),
                listOf(11), mapOf(12 to 13), setOf(14), 15 to 16, Triple(17, 18, 19), Unit,
                arrayOf(20), ubyteArrayOf(21u), ushortArrayOf(22u), uintArrayOf(23u), ULongArray(24),
                25u, 26u, 27u, 28u, 29f, 30.0, mapOf(31 to 32).entries.first(), Clock.System.now(),
                UUID.randomUUID(), 20.milliseconds
            )
        ).isEqualTo(Triple(17, 18, 19))


        expectThat(protocol.genericInline(GenericInline(2))).isEqualTo(GenericInline(2))
        val inlineHolder = InlineHolder2(InlineId(2))
        expectThat(protocol.inlineHolder(inlineHolder)).isEqualTo(inlineHolder)
        expectThat(protocol.typeField(TypeField("wef"))).isEqualTo(TypeField("wef"))


        expectThat(protocol.tree(Tree(2, listOf()))).isEqualTo(Tree(2, listOf()))

        val mutable = MutableThings(mutableMapOf("1" to 2), mutableListOf(3), mutableSetOf(4))
        expectThat(protocol.mutableThings(mutable)).isEqualTo(mutable)
        expectThat(protocol.mutableThingsAsParams(mutableMapOf(1 to 2), mutableListOf(3))).isEqualTo(mutableSetOf(1, 2, 3))

        expectThat(protocol.largeHierarchy(PolymorphicThing.Option1(1))).isEqualTo(PolymorphicThing.Option1(1))

//        expectThat(protocol.defaultValue(WithDefaultValue())).isEqualTo(WithDefaultValue())

//        This is currently bugged, see:
//https://github.com/Kotlin/kotlinx.serialization/issues/2374
//        expectThat(protocol.inlineSealedParent(InlineSealedChild(2))).isEqualTo(InlineSealedChild(2))
//        expectThat(protocol.inlineSealedChild(InlineSealedChild(2))).isEqualTo(InlineSealedChild(2))
//        expectThat(protocol.inlineSealedChildReturnParent(InlineSealedChild(2))).isEqualTo(InlineSealedChild(2))
    }

    fun testParticipants(service: FullService)  = runTest {
        val server = service.server
        val client = service.client
        var actualMessage: String? = null
        val event = client.eventTest("Test string")

        GlobalScope.launch {
            event.collectLatest {
                actualMessage = it
            }
        }

        delay(1000)
        expectThat(actualMessage).isNull()
        server.invokeEventWithParticipants(participant = event.listenerId)
        expectThat(actualMessage).isNull()
        delay(1000)
        expectThat(actualMessage).isNull()
    }
}