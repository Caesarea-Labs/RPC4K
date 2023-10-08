package io.github.natanfudge.rpc4k.processor.utils

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeReference

internal fun KSClassDeclaration.getPublicApiFunctions() = getDeclaredFunctions()
    .filter { !it.isConstructor() && it.isPublic() }

/**
 * Will mark the [KSNode] itself as the cause of the failure if this check fails
 */
context(SymbolProcessorEnvironment)
internal inline fun KSNode.checkRequirement(env: SymbolProcessorEnvironment, requirement: Boolean, msg: () -> String) {
    if (!requirement) env.logger.error(msg(), this)
}

fun KSTypeArgument.nonNullType() =
    type ?: error("There's no reason why a type of a type argument would be null. If you encounter this error, open a bug report ASAP! This happened for '$this'.")