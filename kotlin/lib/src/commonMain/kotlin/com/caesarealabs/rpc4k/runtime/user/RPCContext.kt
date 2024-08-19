package com.caesarealabs.rpc4k.runtime.user

import com.caesarealabs.logging.Logging
import com.caesarealabs.logging.PrintLogging
import com.caesarealabs.rpc4k.runtime.api.SimpleRpcContext

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

    public companion object {
        /**
         * Simple implementation of an [RPCContext] with simple print logging and no additional server data.
         * For more precise context (custom data/logging, etc), use [RPCContext] passed to you by the framework.
         */
        public val Default: RPCContext = SimpleRpcContext(null, PrintLogging)
    }
}

