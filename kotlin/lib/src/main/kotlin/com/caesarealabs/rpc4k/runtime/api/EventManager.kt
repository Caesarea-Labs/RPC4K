package com.caesarealabs.rpc4k.runtime.api


public interface EventConnection {
    public val id: String
    public suspend fun send(bytes: ByteArray)
}

//TODO: I think we should add dropClient to the public API
public interface EventManager<C: EventConnection> {
    public suspend fun subscribe(subscription: EventMessage.Subscribe, connection: C)

    /**
     * Returns whether there was something to actually unsubscribe to matching the criteria.
     */
    public suspend fun unsubscribe(event: String, listenerId: String): Boolean

    /**
     * Returns subscriptions to the given event, watching the given object id
     */
    public suspend fun match(event: String, watchedObjectId: String?): List<EventSubscription<C>>
}

public data class EventSubscription<C: EventConnection>(val connection: C, val info: EventMessage.Subscribe)