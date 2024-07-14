package com.caesarealabs.rpc4k.test;

import com.caesarealabs.rpc4k.runtime.api.C2SEventMessage
import com.caesarealabs.rpc4k.runtime.api.S2CEventMessage
import org.junit.jupiter.api.Test;
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class EventMessageEncodingTest {
    @Test
    fun testSubscribe() {
        val sub = C2SEventMessage.Subscribe("event-123", "waefawef", byteArrayOf(814,114,124,5,5),null)
        expectThat(C2SEventMessage.fromByteArray(sub.toByteArray()) ).isEqualTo(sub)

        val sub2 = C2SEventMessage.Subscribe("event-123", "waefawef", byteArrayOf(814,114,124,5,5),"test 123")
        expectThat(C2SEventMessage.fromByteArray(sub2.toByteArray()) ).isEqualTo(sub2)
    }
    @Test
    fun testUnsubscribe() {
        val sub = C2SEventMessage.Unsubscribe("event-123", "waefawef")
        expectThat(C2SEventMessage.fromByteArray(sub.toByteArray()) ).isEqualTo(sub)
    }
    @Test
    fun testS2C() {
        val emitted = S2CEventMessage.Emitted("ID1234141", "wfe:#!213".toByteArray())
        expectThat(S2CEventMessage.fromString(emitted.toByteArray().decodeToString()) ).isEqualTo(emitted)
        val error = S2CEventMessage.SubscriptionError("waefawef")
        expectThat(S2CEventMessage.fromString(error.toByteArray().decodeToString()) ).isEqualTo(error)

    }
    @Test
    fun testS2CBytes() {
        val emitted = S2CEventMessage.Emitted("ID1234141", "wfe:#!213".toByteArray())
        expectThat(S2CEventMessage.fromByteArray(emitted.toByteArray()) ).isEqualTo(emitted)
        val error = S2CEventMessage.SubscriptionError("waefawef")
        expectThat(S2CEventMessage.fromByteArray(error.toByteArray()) ).isEqualTo(error)

    }

}

private fun byteArrayOf(vararg ints: Int) = byteArrayOf(*ints.map { it.toByte() }.toByteArray())