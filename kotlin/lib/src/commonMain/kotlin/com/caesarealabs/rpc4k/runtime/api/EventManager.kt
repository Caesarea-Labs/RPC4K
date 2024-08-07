package com.caesarealabs.rpc4k.runtime.api

import kotlin.jvm.JvmInline


@JvmInline
public value class EventConnection (public val id: String)

public interface EventManager {
    // TO DO: Handle erroneous event subscriptions
    public suspend fun subscribe(subscription: C2SEventMessage.Subscribe, connection: EventConnection)

    /**
     * Returns whether there was something to actually unsubscribe to matching the criteria.
     */
    public suspend fun unsubscribe(event: String, listenerId: String): Boolean

    /**
     * Returns subscriptions to the given event, watching the given object id
     * @param target If a non-null value is provided then only subscriptions matching that target will be returned
     * if null is passed, all subscriptions of the event will be returned.
     */
    public suspend fun match(event: String, target: String?): List<ManagedEventSubscription>

    /**
     * Removes all subscriptions of the given connection
     */
    public suspend fun dropClient(connection: EventConnection)
}

public data class ManagedEventSubscription(val connection: EventConnection, val info: C2SEventMessage.Subscribe)