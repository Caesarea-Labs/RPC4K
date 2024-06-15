package com.caesarealabs.rpc4k.runtime.api.components

import com.caesarealabs.rpc4k.runtime.api.C2SEventMessage
import com.caesarealabs.rpc4k.runtime.api.EventConnection
import com.caesarealabs.rpc4k.runtime.api.EventManager
import com.caesarealabs.rpc4k.runtime.api.EventSubscription
import com.caesarealabs.rpc4k.runtime.implementation.concurrentAdd
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

internal class MemoryEventManager : EventManager {
    private val subscriptions: MutableMap<String, ConcurrentLinkedQueue<EventSubscription>> = ConcurrentHashMap()

    override suspend fun subscribe(subscription: C2SEventMessage.Subscribe, connection: EventConnection) {
        subscriptions.concurrentAdd(subscription.event, EventSubscription(connection, subscription))
    }

    override suspend fun unsubscribe(event: String, listenerId: String): Boolean {
        val list = subscriptions[event] ?: return false
        return list.removeIf { it.info.listenerId == listenerId }
    }

    override suspend fun dropClient(connection: EventConnection) {
        for (list in subscriptions.values) {
            list.removeIf { it.connection == connection }
        }
    }

    override suspend fun match(event: String, target: String?): List<EventSubscription> {
        val subscribers = subscriptions[event] ?: listOf()
        if (target == null) return subscribers.toList() // No specific object - return all

        // Specific object - return matching
        return subscribers.filter { it.info.target == target }
    }
}