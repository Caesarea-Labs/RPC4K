package io.github.natanfudge.rpc4k.test

//class RealServerTestTemp {
//    private val port = Port(8081)
//    private val config = RealServerTestConfig.create(port, UserProtocol())
//    private val client = config.client
//
//    @Before
//    fun before()  = config.before()
//
//    @After
//    fun after() = config.after()
//
//    @Test
//    fun testUsage() = serverTest {
//        val response = client.createLobby(PlayerId(123), "alo")
//        assertEquals(CreateLobbyResponse(126), response)
//        val response2 = client.killSomeone(111, PlayerId(5), Unit)
//        assertEquals(116.toUInt(), response2)
//        client.someShit(1, 2)
//        client.someShit(1, 2)
//        val mapForEntry = mapOf(1 to 1)
//        client.moreTypes(
//            listOf(),
//            listOf(),
//            1 to 2,
//            Triple(Unit, PlayerId(1), ""),
//            mapForEntry.entries.first()
//        )
//        val result = client.test(1 to 2)
//        assertEquals(Triple(1, 2, "3") to 4.0, result)
//        client.nullable(null, listOf(null))
//        println("fioi")
//        println("fioi")
//        client.someShit(1, 2)
//        val result2 = client.flowTest(2)
//        assertContentEquals(listOf(listOf(PlayerId(2))), result2.toList())
//    }
//
//    @Test
//    fun testInvalidRequest(): Unit = serverTest {
//        val http = OkHttpRpcClient(TestClientLogger, port)
//        assertFailsWith<ExpectationFailedException> {
//            http.request(route = "badroute", ByteArray(0))
//        }
//        assertFailsWith<ExpectationFailedException>("Invalid request encoding") {
//            http.request(route = "/createLobby", ByteArray(0))
//        }
//        val protocol = JvmProtocolFactory.create<UserProtocol>(http = http)
//        val result = protocol.someShit(1, 2)
//        assertEquals("asdf", result)
//    }
//
//
//    @Test
//    fun testServerErrors(): Unit = serverTest {
//        assertFailsWith<InternalServerException> {
//           client.errorTest()
//        }
//
//        assertFailsWith<ExpectationFailedException>("Value must be 2"){
//             client.requirementFail(3)
//        }
//    }
//
//    @Test
//    fun testFlows() = serverTest {
//         client.flowTest(4)
//        client.flowTest(4).first()
//    }
//
//    @Test
//    fun testStateFlows() = serverTest {
//        val response3 = client.sharedFlowTest(4).first()
//        println(response3)
//    }
//
//
//
//}