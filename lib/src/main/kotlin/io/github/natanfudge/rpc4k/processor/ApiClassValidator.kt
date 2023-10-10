package io.github.natanfudge.rpc4k.processor

import com.google.devtools.ksp.isOpen
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*
import io.github.natanfudge.rpc4k.processor.utils.*

/**
 * The methods of this class evaluate all checks to reveal all errors, not just stop at one.
 */
class ApiClassValidator(private val env: SymbolProcessorEnvironment) {

     fun checkApiClassIsValid(apiClass: KSClassDeclaration): Boolean {
        val serializable = apiClass.getPublicApiFunctions().evaluateAll { checkIsSerializable(it) }
        return checkHasCompanionClass(apiClass) && serializable
    }

    /**
     * Api clients should be open (abstract and interface works too) and suspending
     * because the @ApiClient class serves as an interface for the generated client implementation.
     */
     fun checkClassIsSuspendOpen(apiClass: KSClassDeclaration): Boolean {
        val classOpen = checkIsSuspendOpen(apiClass, method = false)
        // Make sure to evaluate all the checks
        return apiClass.getPublicApiFunctions().evaluateAll { checkIsSuspendOpen(it, method = true) } && classOpen
    }

    private fun checkIsSuspendOpen(node: KSDeclaration, method: Boolean): Boolean {
        val messagePrefix = if (method) "Public API method in "
        else ""
        val isOpen = node.isOpen()
        val isSuspending = !method || node.modifiers.contains(Modifier.SUSPEND)

        return when {
            // Make the error messages as descriptive as possible
            !isOpen && !isSuspending -> node.checkRequirement(env ,false) {
                "$messagePrefix@ApiClient class must be suspending and open for inheritance"
            }

            !isOpen -> node.checkRequirement(env, false) {
                "$messagePrefix@ApiClient class must be open for inheritance"
            }

            !isSuspending -> node.checkRequirement(env, false) {
                "$messagePrefix@ApiClient class must be suspending"
            }

            else -> true
        }
    }



    private fun checkIsSerializable(function: KSFunctionDeclaration): Boolean {
        val returnSerializable = checkIsSerializable(function.nonNullReturnType())
        // Make sure to evaluate all the checks
        return function.parameters.evaluateAll { checkIsSerializable(it.type) } && returnSerializable
    }

    private fun checkHasCompanionClass(apiClass: KSClassDeclaration): Boolean {
        return apiClass.checkRequirement(env, apiClass.declarations.any { it is KSClassDeclaration && it.isCompanionObject }) {
            "Api class must contain a companion object"
        }
    }

    private fun checkIsSerializable(type: KSTypeReference, target: KSNode = type, typeArgument: Boolean = false): Boolean {
        val resolved = type.resolve()
        val selfSerializable = target.checkRequirement(env, resolved.isSerializable()) {
            "Type used in API method '${resolved.declaration.qualifiedName!!.asString()}' must be Serializable".appendIf(typeArgument) { " (in type argument of $target)" }
        }
        // Make sure to evaluate all the checks
        return resolved.arguments.evaluateAll { checkIsSerializable(it.nonNullType(), target = target, typeArgument = true) } && selfSerializable
    }
}

/**
 * Checks [condition] for ALL elements of this, even though this is slower, to give the user all the possible errors.
 */
private fun <T> Iterable<T>.evaluateAll(condition: (T) -> Boolean): Boolean {
    var failed = false
    for (item in this) {
        if (!condition(item)) failed = true
    }
    return !failed
}

/**
 * Checks [condition] for ALL elements of this, even though this is slower, to give the user all the possible errors.
 */
private fun <T> Sequence<T>.evaluateAll(condition: (T) -> Boolean): Boolean {
    var failed = false
    for (item in this) {
        if (!condition(item)) failed = true
    }
    return !failed
}