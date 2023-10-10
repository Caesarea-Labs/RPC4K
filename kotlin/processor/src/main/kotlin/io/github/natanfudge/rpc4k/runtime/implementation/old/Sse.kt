package io.github.natanfudge.rpc4k.runtime.implementation.old

/**
 * The data class representing a SSE Event that will be sent to the client.
 */
data class Sse (val data: String, val event: String? = null, val id: String? = null)