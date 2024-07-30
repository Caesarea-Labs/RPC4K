package com.caesarealabs.rpc4k.runtime.user

import com.caesarealabs.logging.Logging

/**
 * Additional context available to any RPC function that is specific to that function.
 *
 * Allows logging in a way that is scoped to the specific RPC function (call the [Logging] logX methods).
 */
public interface RPCContext: Logging {
    /**
     * This is an object passed by your server implementation. To use it, consult the server's documentation.
     */
    public val serverData: Any?
}

