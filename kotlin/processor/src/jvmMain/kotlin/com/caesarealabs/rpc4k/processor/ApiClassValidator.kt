@file:OptIn(KspExperimental::class)

package com.caesarealabs.rpc4k.processor

import com.caesarealabs.rpc4k.processor.utils.*
import com.caesarealabs.rpc4k.runtime.user.Dispatch
import com.caesarealabs.rpc4k.runtime.user.EventTarget
import com.caesarealabs.rpc4k.runtime.user.RpcEvent
import com.google.devtools.ksp.*
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*
import kotlinx.serialization.Contextual

private const val TypeDiscriminatorField = "type"

/**
 * The methods of this class evaluate all checks to reveal all errors, not just stop at one.
 */
internal class ApiClassValidator(private val env: SymbolProcessorEnvironment, private val resolver: Resolver) {
    fun validate(apiClass: KSClassDeclaration): Boolean {
//        if (!apiClass.validate()) return false
        var valid = checkApiClassIsValid(apiClass)
//        if (apiClass.shouldGenerateClient()) {
//            // Servers don't need to be suspendOpen, only clients
//            valid = checkClassIsSuspendOpen(apiClass) && valid
//        }

        var unserializableReferencedClass = false

        val referencedClasses = apiClass.getReferencedClasses(resolver) {
            val type = it.resolveToUnderlying()
            // Type parameters get a pass since they might get expanded into something serializable
            if (type.declaration is KSTypeParameter) return@getReferencedClasses true
            val serializable = type.isSerializable()
            if (!serializable) unserializableReferencedClass = true
            it.checkRequirement(env, serializable) {
                "Referenced type '${type.declaration.getQualifiedName()}' is not a @Serializable class or a builtin serializable type."
            }
        }

        // Make sure to invoke all the checks so the user will get all the errors at once
        val noSealedClasses = checkNoGenericSealedClasses(referencedClasses)
        val hasContextualWhenNeeded = checkContextualAnnotationOnCertainPropertyTypes(referencedClasses)
        val notUsingReservedPropertyName = checkNoTypePropertyOnSealedSubclasses(referencedClasses)
        return valid && noSealedClasses && hasContextualWhenNeeded && notUsingReservedPropertyName && !unserializableReferencedClass
    }

    /**
     * In sealed subclasses we use a 'type' field to identify its type in network form, so we can't allow users to use that property name.
     */
    private fun checkNoTypePropertyOnSealedSubclasses(referencedClasses: Set<KSClassDeclaration>): Boolean {
        return referencedClasses.filter { it.isSealedSubclass() }.evaluateAll { classDecl ->
            classDecl.getDeclaredProperties().evaluateAll { property ->
                property.checkRequirement(env, property.getSimpleName() != TypeDiscriminatorField) {
                    "The name '$TypeDiscriminatorField' is reserved for sealed classes"
                }
            }
        }
    }

    private fun KSClassDeclaration.isSealedSubclass() = getAllSuperTypes().any { Modifier.SEALED in it.declaration.modifiers }


    /**
     * For [Pair], [Triple], [Map.Entry] and [Unit] we have special serializers.
     * Currently there's no way to automatically force kotlinx.serialization to use those serializers for class properties, so we force
     * the user to use @Contextual on them.
     */
    private fun checkContextualAnnotationOnCertainPropertyTypes(referencedClasses: Set<KSClassDeclaration>): Boolean {
        // Blocked: this is not needed as soon as we can force kotlinx.serialization to use our serializers with a compiler plugin
        // Blocked: Also we need to get rid of the test that verifies this in that case
        return referencedClasses.evaluateAll { classDecl ->
            classDecl.getDeclaredProperties().evaluateAll { property ->
                val typeName = property.type.resolve().declaration.getQualifiedName()
                if (typeName in typesWithCustomSerializers) {
                    property.type.checkRequirement(env, property.annotatedByContextual() || property.type.annotatedByContextual()) {
                        "@Contextual must be specified on properties of type $typeName"
                    }
                } else {
                    true
                }

            }
        }
    }

    private fun KSAnnotated.annotatedByContextual() = hasAnnotation(Contextual::class)

    private val typesWithCustomSerializers = setOf(
        "kotlin.Pair",
        "kotlin.Triple",
        "kotlin.collections.Map.Entry",
        "kotlin.Unit"
    )


    /**
     *      A normal union looks like this:
     *      ```
     *      type Foo<T> = Something<T> | SomethingElse
     *      interface Something<T>
     *      interface SomethingElse
     *      ```
     *      But in Kotlin it looks like this:
     *      ```
     *      sealed interface Foo<T> {
     *          class Something<T> : Something<T>
     *          class SomethingElse: Something<Int>
     *     }
     *     ```
     *     So the sealed interface model for generic types doesn't really fit well with the union type model.
     *     Generic types could be implemented by adding inheritance to the format, but I don't think that's a good idea.
     *
     */
    private fun checkNoGenericSealedClasses(referencedClasses: Set<KSClassDeclaration>): Boolean {
        return referencedClasses.evaluateAll {
            it.checkRequirement(env, it.typeParameters.isEmpty() || it.fastGetSealedSubclasses(resolver).toList().isEmpty()) {
                "Generic sealed classes are not supported in RPC4K. The concept doesn't fit well with other languages and kotlinx.serialization breaks with them anyway."
            }
        }
    }

    private fun checkApiClassIsValid(apiClass: KSClassDeclaration): Boolean {
        val serializable = apiClass.getPublicApiFunctions().evaluateAll {
            val serializable = checkIsSerializable(it)
            checkAnnotationsAreValid(it) && serializable
        }
        return checkHasCompanionClass(apiClass) && serializable
    }

    /**
     * Api clients should be open (abstract and interface works too) and suspending
     * because the @ApiClient class serves as an interface for the generated client implementation.
     */
    private fun checkClassIsSuspendOpen(apiClass: KSClassDeclaration): Boolean {
//        apiClass.primaryConstructor?.let { ctr ->
//            ctr.parameters.evaluateAll {
//                it.checkRequirement(env, it.hasDefault) {
//                    // The generated client class extends the user's class, and having required parameters for the user's class would make it impossible
//                    // to simply extend it (we would need to specify some value)
//                    "@Api client class must have default values for its primary constructor"
//                }
//            }
//        }
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
            !isOpen && !isSuspending -> node.checkRequirement(env, false) {
                "$messagePrefix@Api client class must be suspending and open for inheritance"
            }

            // RpcEvents don't need to be open because they are not invoked in the same way they are declared
            !isOpen && !node.isAnnotationPresent(RpcEvent::class) -> node.checkRequirement(env, false) {
                "$messagePrefix@Api client class must be open for inheritance"
            }

            !isSuspending -> node.checkRequirement(env, false) {
                "$messagePrefix@Api client class must be suspending"
            }

            else -> true
        }
    }

    //TODO: test these validations
    private fun checkAnnotationsAreValid(function: KSFunctionDeclaration): Boolean {
        if (function.isAnnotationPresent(RpcEvent::class)) {
            val annotatedWithTargetCount = function.parameters.count {
                it.isAnnotationPresent(
                    EventTarget::class
                )
            }
            function.checkRequirement(env, annotatedWithTargetCount <= 1) {
                "only one parameter may be annotated with @EventTarget"
            }
            return function.parameters.evaluateAll {
                val annotatedByBoth = it.isAnnotationPresent(EventTarget::class) && it.isAnnotationPresent(
                    Dispatch::class
                )
                it.checkRequirement(env, !annotatedByBoth) {
                    "@Dispatch and @EventTarget are mutually exclusive"
                }
            }
        } else {
            // No RpcEvent - disallow @Target/@Dispatch
            return function.parameters.evaluateAll {
                val target = it.checkRequirement(env, !it.isAnnotationPresent(EventTarget::class)) {
                    "@EventTarget is only relevant on functions annotated with @RpcEvent"
                }
                val dispatch = it.checkRequirement(env, !it.isAnnotationPresent(Dispatch::class)) {
                    "@Dispatch is only relevant on functions annotated with @RpcEvent"
                }
                target && dispatch
            }
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
        val resolved = type.resolveToUnderlying()

        if (!target.checkRequirement(env, resolved.declaration.qualifiedName != null) {
                "Cannot parse type"
            }) return false


        val selfSerializable = target.checkRequirement(env, resolved.isSerializable()) {
            "Type used in API method '${resolved.declaration.qualifiedName!!.asString()}' must be Serializable. Serializable types: $builtinSerializableClasses"
                .appendIf(typeArgument) { " (in type argument of $target)" }
        }
        // Make sure to evaluate all the checks
        return resolved.arguments.evaluateAll { checkIsSerializable(it.nonNullType(), target = target, typeArgument = true) } && selfSerializable
    }
}

/**
 * Checks [condition] for ALL elements of this, even though this is slower, to give the user all the possible errors.
 */
private inline fun <T> Iterable<T>.evaluateAll(condition: (T) -> Boolean): Boolean {
    var failed = false
    for (item in this) {
        if (!condition(item)) failed = true
    }
    return !failed
}

/**
 * Checks [condition] for ALL elements of this, even though this is slower, to give the user all the possible errors.
 */
private inline fun <T> Sequence<T>.evaluateAll(condition: (T) -> Boolean): Boolean {
    var failed = false
    for (item in this) {
        if (!condition(item)) failed = true
    }
    return !failed
}