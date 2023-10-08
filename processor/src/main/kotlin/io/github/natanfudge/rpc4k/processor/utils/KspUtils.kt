package io.github.natanfudge.rpc4k.processor.utils

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode

internal fun KSClassDeclaration.getPublicApiFunctions() = getDeclaredFunctions()
    .filter { !it.isConstructor() && it.isPublic() }

/**
 * Will mark the [KSNode] itself as the cause of the failure if this check fails
 */
context(SymbolProcessorEnvironment)
internal inline fun KSNode.checkRequirement(env: SymbolProcessorEnvironment, requirement: Boolean, msg: () -> String) {
    if (!requirement) env.logger.error(msg(), this)
}