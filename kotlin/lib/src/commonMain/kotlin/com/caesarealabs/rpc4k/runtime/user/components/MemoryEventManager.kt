package com.caesarealabs.rpc4k.runtime.user.components

import com.caesarealabs.rpc4k.runtime.api.C2SEventMessage
import com.caesarealabs.rpc4k.runtime.api.EventConnection
import com.caesarealabs.rpc4k.runtime.api.EventManager
import com.caesarealabs.rpc4k.runtime.api.ManagedEventSubscription
import com.caesarealabs.rpc4k.runtime.implementation.concurrentAdd
import com.caesarealabs.rpc4k.runtime.platform.ConcurrentMutableMap

internal class MemoryEventManager : EventManager {
    /**
     * Map from event to its subscribers
     */
    private val subscriptions: MutableMap<String, MutableCollection<ManagedEventSubscription>> = ConcurrentMutableMap()

    override suspend fun subscribe(subscription: C2SEventMessage.Subscribe, connection: EventConnection) {
        subscriptions.concurrentAdd(subscription.event, ManagedEventSubscription(connection, subscription))
    }

    override suspend fun unsubscribe(event: String, listenerId: String): Boolean {
        val list = subscriptions[event] ?: return false
        return list.removeAll { it.info.listenerId == listenerId }
    }

    override suspend fun dropClient(connection: EventConnection) {
        for (list in subscriptions.values) {
            list.removeAll { it.connection == connection }
        }
    }

    override suspend fun match(event: String, target: String?): List<ManagedEventSubscription> {
        val subscribers = subscriptions[event] ?: listOf()
        if (target == null) return subscribers.toList() // No specific object - return all

        // Specific object - return matching
        return subscribers.filter { it.info.target == target }
    }
}