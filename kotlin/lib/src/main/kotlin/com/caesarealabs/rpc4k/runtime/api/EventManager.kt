package com.caesarealabs.rpc4k.runtime.api


public interface EventConnection {
    public val id: String
    public suspend fun send(bytes: ByteArray)
}

public interface EventManager<C: EventConnection> {
    //TODO: consider validating subscriptions. Maybe benchmark how much time it takes. It's a good idea for correctness
    // but not that important.
    public suspend fun subscribe(subscription: EventMessage.Subscribe, connection: C)

    /**
     * Returns whether there was something to actually unsubscribe to matching the criteria.
     */
    public suspend fun unsubscribe(event: String, listenerId: String): Boolean

    /**
     * Returns subscriptions to the given event, watching the given object id
     * @param target If a non-null value is provided then only subscriptions matching that target will be returned
     * if null is passed, all subscriptions of the event will be returned.
     */
    public suspend fun match(event: String, target: String?): List<EventSubscription<C>>

    /**
     * Removes all subscriptions of the given connection
     */
    public suspend fun dropClient(connection: C)
}

public data class EventSubscription<C: EventConnection>(val connection: C, val info: EventMessage.Subscribe)