package io.github.natanfudge.rpc4k.processor.utils

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*
import io.github.natanfudge.rpc4k.runtime.implementation.KotlinClassName
import kotlin.reflect.KClass

/**
 * Asserts that only classes have this annotation. Make sure the annotation is declared to only support class use site.
 */
@Suppress("UNCHECKED_CAST")
fun Resolver.getClassesWithAnnotation(annotation: KClass<*>): Sequence<KSClassDeclaration> = getSymbolsWithAnnotation(annotation.qualifiedName!!)
        as Sequence<KSClassDeclaration>


internal fun KSClassDeclaration.getPublicApiFunctions() = getDeclaredFunctions()
    .filter { !it.isConstructor() && it.isPublic() }

//fun KSDeclaration.getSimpleName() = simpleName.asString()

/**
 * Extract from `com.foo.bar.Inner$Thing` the pair `[com.foo.bar, inner.Thing]`
 */
//fun KSDeclaration.getPackageAndClassName(): Pair<String, String> {
//    val qualifiedName = nonNullQualifiedName()
//    val packageName = packageName.asString()
//    val className = qualifiedName.removePrefix("$packageName.")
//    return packageName to className
//}
/**
 * Extract from `com.foo.bar.Inner$Thing` the pair `[com.foo.bar, inner.Thing]`
 */
fun KSDeclaration.getKotlinName(): KotlinClassName {
    val qualifiedName = nonNullQualifiedName()
    val packageName = packageName.asString()
    val className = qualifiedName.removePrefix("$packageName.")
    return KotlinClassName(pkg = packageName, simple = className)
}

/**
 * Extract from `com.foo.bar.Inner$Thing` , `Inner.Thing`
 */
fun KSDeclaration.getSimpleName(): String = if (this is KSClassDeclaration) {
    val qualifiedName = nonNullQualifiedName()
    val packageName = packageName.asString()
    qualifiedName.removePrefix("$packageName.")
} else {
    getTopLevelSimpleName()
}

/**
 * Extract from `com.foo.bar.Inner$Thing` , `Thing`
 */
fun KSDeclaration.getTopLevelSimpleName(): String = simpleName.asString()


/**
 * Will mark the [KSNode] itself as the cause of the failure if this check fails
 */
internal inline fun KSNode.checkRequirement(environment: SymbolProcessorEnvironment, requirement: Boolean, msg: () -> String): Boolean {
    if (!requirement) environment.logger.error(msg(), this)
    return requirement
}

fun KSTypeArgument.nonNullType() =
    type
        ?: error(
            "There's no reason why a type of a type argument would be null. If you encounter this error, open a bug report ASAP! This happened for '$this'."
        )

fun KSDeclaration.nonNullQualifiedName() =
    qualifiedName?.asString()
        ?: error(
            "There's no reason why the qualified name of a type would be null. If you encounter this error, open a bug report ASAP! This happened for '$this'."
        )

fun KSFunctionDeclaration.nonNullReturnType() =
    returnType
        ?: error(
            "There's no reason why the return type of a function would be null. If you encounter this error, open a bug report ASAP! This happened for '$this'."
        )

/**
 * Handles type aliases as well
 */
fun KSTypeReference.resolveToUnderlying(): KSType {
    var candidate = resolve()
    var declaration = candidate.declaration
    while (declaration is KSTypeAlias) {
        candidate = declaration.type.resolve()
        declaration = candidate.declaration
    }
    return candidate
}


fun CodeGenerator.writeFile(
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
fun KSClassDeclaration.getReferencedClasses(filter: (KSTypeReference) -> Boolean = { true }): Set<KSClassDeclaration> {
    val types = hashSetOf<KSClassDeclaration>()
    // Add things referenced in methods
    for (method in getPublicApiFunctions()) {
        addReferencedTypes(method.nonNullReturnType(), types, filter)
        for (arg in method.parameters) {
            addReferencedTypes(arg.type, types, filter)
        }
    }

    return types
}


//TODO: test recursive references
/**
 * When you have a reference like
 * ```
 * class SomeClass<SomeOtherClass> {
 *     val anotherThing: AnotherClass
 * }
 * ```
 * It will return everything referenced by `SomeClass`, including `SomeOtherClass` and `AnotherClass`.
 */
private fun addReferencedTypes(type: KSTypeReference, addTo: MutableSet<KSClassDeclaration>, filter: (KSTypeReference) -> Boolean) {
    if (!filter(type)) return
    val resolved = type.resolveToUnderlying()
    for (typeArg in resolved.arguments) {
        if (typeArg.type != null) addReferencedTypes(typeArg.type!!, addTo, filter)
    }
    val declaration = resolved.declaration
    // We really don't want to iterate over builtin types, and we need to be careful to only process everything once or this will be infinite recursion.
    if (!resolved.isBuiltinSerializableType() && declaration !in addTo && declaration is KSClassDeclaration) {
        for (arg in resolved.arguments) {
            addReferencedTypes(arg.nonNullType(), addTo, filter)
        }
        addReferencedTypes(declaration, addTo, filter)
    }
}

private fun addReferencedTypes(declaration: KSClassDeclaration, addTo: MutableSet<KSClassDeclaration>, filter: (KSTypeReference) -> Boolean) {
    addTo.add(declaration)
    // Include types referenced in properties of models as well
    for (property in declaration.getAllProperties()) {
        addReferencedTypes(property.type, addTo, filter)
    }
    // Add sealed subclasses as well
    for (sealedSubClass in declaration.getSealedSubclasses()) {
        addReferencedTypes(sealedSubClass, addTo, filter)
    }
}