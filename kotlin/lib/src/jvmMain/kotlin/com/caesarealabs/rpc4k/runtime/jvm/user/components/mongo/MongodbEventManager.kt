package com.caesarealabs.rpc4k.runtime.jvm.user.components.mongo

import com.caesarealabs.rpc4k.runtime.api.C2SEventMessage
import com.caesarealabs.rpc4k.runtime.api.EventConnection
import com.caesarealabs.rpc4k.runtime.api.EventManager
import com.caesarealabs.rpc4k.runtime.api.ManagedEventSubscription
import com.mongodb.client.model.Filters
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.Serializable

/**
 * Stores and manages events as an [EventManager] with a [mongoDb] database.
 * @param databaseName The database under which events will be managed (will be created automatically)
 */
public class MongodbEventManager(private val mongoDb: MongoDb, databaseName: String = "rpc4k") : EventManager {
    /** getOrCreateClient() can be very expensive to lazily call it */
    private val client by lazy { mongoDb.getOrCreateClient() }

    /** Lazyness is needed to avoid referencing [client] */
    private val rpc4kDatabase by lazy { client.getDatabase(databaseName) }

    /** Lazyness is needed to avoid referencing [rpc4kDatabase] and then [client] */
    private val eventsCollection by lazy { rpc4kDatabase.getCollection<MongodbEventSubscription>("events") }

    /**
     * Stores a subscription by inserting to a mongoDB database
     */
    override suspend fun subscribe(subscription: C2SEventMessage.Subscribe, connection: EventConnection) {
        //TODO: make sure to handle indices correctly in regards to events and @WatchedValues
        eventsCollection.insertOne(
            MongodbEventSubscription(subscription.listenerId, subscription.data, subscription.target, connection.id, subscription.event)
        )
    }

    /**
     * Remove a subscription by removing from a mongoDB database
     */
    override suspend fun unsubscribe(event: String, listenerId: String): Boolean {
        val result = eventsCollection.deleteOne(Filters.eq(MongodbEventSubscription::listenerId.name, listenerId))
        return result.deletedCount > 0
    }

    /**
     * Efficiently finds subscription to the [event] with the given [target].
     */
    override suspend fun match(event: String, target: String?): List<ManagedEventSubscription> {
        // These 2 are indexed so this eq search is fast
        val eventFilter = Filters.eq(MongodbEventSubscription::event.name, event)
        // If this event doesn't supports targets, don't take it into account in the query.
        val query = if (target == null) eventFilter else Filters.and(Filters.eq(MongodbEventSubscription::target.name, target), eventFilter)
        val result = try {
            eventsCollection.find(query).toList()
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }
        return result
            .map {
                ManagedEventSubscription(
                    EventConnection(it.connectionId), C2SEventMessage.Subscribe(event, it.listenerId, it.data, it.target)
                )
            }
    }

    /**
     * Get rid of all entries matching the [connection] id
     */
    override suspend fun dropClient(connection: EventConnection) {
        println("Dropped")
        eventsCollection.deleteMany(Filters.eq(MongodbEventSubscription::connectionId.name, connection.id))
    }
}

/**
 * Rows stored
 */
@Serializable private class MongodbEventSubscription(
    /**
     * @see C2SEventMessage.Subscribe.listenerId
     */
    val listenerId: String,
    /**
     * @see C2SEventMessage.Subscribe.data
     */
    val data: ByteArray,
    //TODO: make sure to index correctly
    /**
     * @see C2SEventMessage.Subscribe.target
     */
    val target: String?,
    /**
     * The connection with the single client currently trying to get events for this [event] with the given [target].
     */
    val connectionId: String,
    //TODO: make sure to index correctly
    /**
     * Unique identifier for the event that this subscribes to. This matches a function name declared in the RPC class.
     */
    val event: String
)