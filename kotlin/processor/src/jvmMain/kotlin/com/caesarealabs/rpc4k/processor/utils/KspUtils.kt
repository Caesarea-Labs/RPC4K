package com.caesarealabs.rpc4k.processor.utils

import com.google.devtools.ksp.*
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*
import com.caesarealabs.rpc4k.runtime.implementation.KotlinClassName
import kotlin.reflect.KClass

/**
 * Asserts that only classes have this annotation. Make sure the annotation is declared to only support class use site.
 */
@Suppress("UNCHECKED_CAST")
internal fun Resolver.getClassesWithAnnotation(annotation: KClass<*>): Sequence<KSClassDeclaration> = getSymbolsWithAnnotation(annotation.qualifiedName!!)
        as Sequence<KSClassDeclaration>


internal fun KSClassDeclaration.getPublicApiFunctions() = getDeclaredFunctions()
    .filter { !it.isConstructor() && it.isPublic() }

/**
 * Only checks the short name of annotations for performance, may not work well with annotation name conflicts.
 */
internal fun KSAnnotated.hasAnnotation(annotation: KClass<*>) = annotations.any { it.shortName.asString() == annotation.simpleName }


/**
 * Extract from `com.foo.bar.Inner$Thing` the pair `[com.foo.bar, inner.Thing]`
 */
internal fun KSDeclaration.getKotlinName(): KotlinClassName? {
    val qualifiedName = getQualifiedName()
    val packageName = packageName.asString()
    val className = qualifiedName?.removePrefix("$packageName.") ?: return null
    return KotlinClassName(pkg = packageName, simple = className)
}

/**
 * Extract from `com.foo.bar.Inner$Thing` , `Inner.Thing`
 */
internal fun KSDeclaration.getSimpleName(): String? = if (this is KSClassDeclaration) {
    val qualifiedName = getQualifiedName()
    val packageName = packageName.asString()
    qualifiedName?.removePrefix("$packageName.")
} else {
    getTopLevelSimpleName()
}

/**
 * Extract from `com.foo.bar.Inner$Thing` , `Thing`
 */
internal fun KSDeclaration.getTopLevelSimpleName(): String = simpleName.asString()


/**
 * Will mark the [KSNode] itself as the cause of the failure if this check fails
 * Returns [requirement]
 */
internal inline fun KSNode.checkRequirement(environment: SymbolProcessorEnvironment, requirement: Boolean, msg: () -> String): Boolean {
    if (!requirement) environment.logger.error(msg(), this)
    return requirement
}

internal fun KSTypeArgument.nonNullType() =
    type
        ?: error(
            "There's no reason why a type of a type argument would be null. If you encounter this error, open a bug report ASAP! This happened for '$this'."
        )

internal fun KSDeclaration.getQualifiedName() =
    qualifiedName?.asString()
//        ?: error(
//            "There's no reason why the qualified name of a type would be null. If you encounter this error, open a bug report ASAP! This happened for '$this'."
//        )

internal fun KSFunctionDeclaration.nonNullReturnType() =
    returnType
        ?: error(
            "There's no reason why the return type of a function would be null. If you encounter this error, open a bug report ASAP! This happened for '$this'."
        )

/**
 * Handles type aliases as well
 */
internal fun KSTypeReference.resolveToUnderlying(): KSType {
    var candidate = resolve()
    var declaration = candidate.declaration
    while (declaration is KSTypeAlias) {
        candidate = declaration.type.resolve()
        declaration = candidate.declaration
    }
    return candidate
}


internal fun CodeGenerator.writeFile(
    contents: String,
    dependencies: Dependencies,
    path: String,
    extensionName: String = "kt"
) {
    createNewFileByPath(dependencies, path, extensionName).use { it.write(contents.toByteArray()) }
}

/**
 * When you have methods like
 * ```
 * fun foo(param: SomeClass)
 * ```
 *
 * It will return everything referenced by `SomeClass` and other such referenced classes.
 *
 * @param filter Allows denying certain classes, which will make it so things they have referenced will not get visited.
 */
internal fun KSClassDeclaration.getReferencedClasses(resolver: Resolver, filter: (KSTypeReference) -> Boolean = { true }): Set<KSClassDeclaration> {
    val types = hashSetOf<KSClassDeclaration>()
    // Add things referenced in methods
    for (method in getPublicApiFunctions()) {
        addReferencedTypes(method.nonNullReturnType(), types, resolver, filter)
        for (arg in method.parameters) {
            addReferencedTypes(arg.type, types, resolver, filter)
        }
    }

    return types
}


/**
 * When you have a reference like
 * ```
 * class SomeClass<SomeOtherClass> {
 *     val anotherThing: AnotherClass
 * }
 * ```
 * It will return everything referenced by `SomeClass`, including `SomeOtherClass` and `AnotherClass`.
 */
private fun addReferencedTypes(
    type: KSTypeReference,
    addTo: MutableSet<KSClassDeclaration>,
    resolver: Resolver,
    filter: (KSTypeReference) -> Boolean
) {
    if (!filter(type)) return
    val resolved = type.resolveToUnderlying()
    for (typeArg in resolved.arguments) {
        if (typeArg.type != null) addReferencedTypes(typeArg.type!!, addTo, resolver, filter)
    }
    val declaration = resolved.declaration
    // We really don't want to iterate over builtin types, and we need to be careful to only process everything once or this will be infinite recursion.
    if (!resolved.isBuiltinSerializableType() && declaration !in addTo && declaration is KSClassDeclaration) {
        for (arg in resolved.arguments) {
            addReferencedTypes(arg.nonNullType(), addTo, resolver, filter)
        }
        addReferencedTypes(declaration, addTo, resolver, filter)
    }
}

private fun addReferencedTypes(
    declaration: KSClassDeclaration,
    addTo: MutableSet<KSClassDeclaration>,
    resolver: Resolver,
    filter: (KSTypeReference) -> Boolean
) {
    addTo.add(declaration)
    // Include types referenced in properties of models as well
    for (property in declaration.getAllProperties()) {
        addReferencedTypes(property.type, addTo, resolver, filter)
    }
    // Add sealed subclasses as well
    for (sealedSubClass in declaration.fastGetSealedSubclasses(resolver)) {
        addReferencedTypes(sealedSubClass, addTo, resolver, filter)
    }
}


/**
 * For some reason, when you call getSealedSubclasses it invalidates the ksp caches and basically forces ksp to reprocess everything every time.
 * This is a known limitation and may be fixed in KSP 2.
 * This version does not cause the cache to be invalidated.
 *
 */
@OptIn(KspExperimental::class) internal fun KSClassDeclaration.fastGetSealedSubclasses(resolver: Resolver): Sequence<KSClassDeclaration> {
    if (Modifier.SEALED !in modifiers) return emptySequence()
    val packageName = containingFile?.packageName ?: return emptySequence()

    val filesInSamePackage = resolver.getDeclarationsFromPackage(packageName.asString())
    return filesInSamePackage.filterIsInstance<KSClassDeclaration>()
        .flatMap { it.getAllDeclarations() }
        .filterIsInstance<KSClassDeclaration>()
        .filter { superType -> superType.getAllSuperTypes().any { it.declaration == this } }
}



private fun KSClassDeclaration.getAllDeclarations(): Sequence<KSDeclaration> {
    return sequence {
        yield(this@getAllDeclarations)
        for (declaration in declarations) {
            if (declaration is KSClassDeclaration) {
                yieldAll(declaration.getAllDeclarations())
            }
        }
    }
}