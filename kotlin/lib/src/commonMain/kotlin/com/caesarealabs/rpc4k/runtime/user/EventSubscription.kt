package com.caesarealabs.rpc4k.runtime.user

import kotlinx.coroutines.flow.Flow

/**
 * A [Flow], that additionally exposes the listener ID of the event subscription.
 */
public class EventSubscription<T>(public val listenerId: String, private val flow: Flow<T>): Flow<T> by flow