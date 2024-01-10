package com.caesarealabs.rpc4k.test;

import com.caesarealabs.rpc4k.runtime.api.EventMessage
import org.junit.jupiter.api.Test;
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class EventMessageEncodingTest {
    @Test
    fun testSubscribe() {
        val sub = EventMessage.Subscribe("event-123", "waefawef", byteArrayOf(814,114,124,5,5),null)
        expectThat(EventMessage.fromByteArray(sub.toByteArray()) ).isEqualTo(sub)

        val sub2 = EventMessage.Subscribe("event-123", "waefawef", byteArrayOf(814,114,124,5,5),"test 123")
        expectThat(EventMessage.fromByteArray(sub2.toByteArray()) ).isEqualTo(sub2)
    }
    @Test
    fun testUnsubscribe() {
        val sub = EventMessage.Unsubscribe("event-123", "waefawef")
        expectThat(EventMessage.fromByteArray(sub.toByteArray()) ).isEqualTo(sub)
    }
}

private fun byteArrayOf(vararg ints: Int) = byteArrayOf(*ints.map { it.toByte() }.toByteArray())