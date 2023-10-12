package io.github.natanfudge.rpc4k.processor

import com.google.devtools.ksp.symbol.*
import io.github.natanfudge.rpc4k.processor.utils.*

object KspToApiDefinition {
    fun toApiDefinition(kspClass: KSClassDeclaration): ApiDefinition {
        val (packageName, className) = kspClass.getPackageAndClassName()
        return ApiDefinition(
            // Doesn't quite fit, but good enough to represent the name as an RpcClass
            name = RpcClass(packageName = packageName, simpleName = className, isNullable = false, typeArguments = listOf()),
            methods = kspClass.getPublicApiFunctions().map { toRpc(it) }.toList(),
            getRpcModels(kspClass)
        )
    }

    private fun toRpc(kspMethod: KSFunctionDeclaration): RpcDefinition {
        return RpcDefinition(
            kspMethod.simpleName.getShortName(),
            parameters = kspMethod.parameters.map { toRpcParameter(it) }.toList(),
            returnType = toRpcClass(kspMethod.nonNullReturnType())
        )
    }

    private fun toRpcParameter(argument: KSValueParameter): RpcParameter {
        return RpcParameter(
            name = argument.name?.getShortName() ?: error("Only named parameters are expected at the moment"),
            type = toRpcClass(argument.type)
        )
    }


    /**
     * Get list of models like this:
     * ```
     * MyClass {
     *      val x: Int
     *      val y: Int
     * }
     * ```
     * as [RpcModel]s
     */
    private fun getRpcModels(kspClass: KSClassDeclaration): List<RpcModel> {
        return getReferencedTypes(kspClass).map { declaration ->
            RpcModel(
                name = declaration.simpleName.asString(),
                typeParameters = declaration.typeParameters.map { it.name.asString() },
                properties = declaration.getAllProperties().map {
                    it.simpleName.asString() to toRpcType(it.type)
                }.toMap()
            )
        }
    }

    /**
     * Get `MyClass<Int,T>` as an [RpcType]
     * */
    private fun toRpcType(reference: KSTypeReference): RpcType {
        val type = reference.resolve()
        val declaration = type.declaration
        val (_, className) = declaration.getPackageAndClassName()
        return RpcType(
            name = if (declaration is KSTypeParameter) declaration.name.getShortName() else className,
            isTypeParameter = declaration is KSTypeParameter,
            typeArguments = type.arguments.map { toRpcType(it.nonNullType()) }
        )
    }

    /**
     * Get `com.foo.bar.MyClass<Int,String>` as an [RpcClass]
     * */
    private fun toRpcClass(type: KSTypeReference): RpcClass {
        val resolved = type.resolve()
        val (packageName, className) = resolved.declaration.getPackageAndClassName()
        return RpcClass(
            packageName = packageName, simpleName = className,
            typeArguments = resolved.arguments.map { toRpcClass(it.nonNullType()) },
            isNullable = resolved.isMarkedNullable
        )
    }

    /**
     * Extract from `com.foo.bar.Inner$Thing` the pair `[com.foo.bar, inner.Thing]`
     */
    private fun KSDeclaration.getPackageAndClassName(): Pair<String, String> {
        val qualifiedName = nonNullQualifiedName()
        val packageName = packageName.asString()
        val className = qualifiedName.removePrefix("$packageName.")
        return packageName to className
    }

    /**
     * When you have methods like
     * ```
     * fun foo(param: SomeClass)
     * ```
     *
     * It will return everything referenced by `SomeClass` and other such referenced classes.
     */
    private fun getReferencedTypes(kspClass: KSClassDeclaration): Set<KSClassDeclaration> {
        val types = hashSetOf<KSClassDeclaration>()
        for (method in kspClass.getPublicApiFunctions()) {
            addReferencedTypes(method.nonNullReturnType(), types)
            for (arg in method.parameters) {
                addReferencedTypes(arg.type, types)
            }
        }
        return types
    }

    //TODO: test recursive references
    /**
     * When you have a reference like
     * ```
     * SomeClass<SomeOtherClass> {
     *     val anotherThing: AnotherClass
     * }
     * ```
     * It will return everything referenced by `SomeClass`, including `SomeOtherClass` and `AnotherClass`.
     */
    private fun addReferencedTypes(type: KSTypeReference, addTo: MutableSet<KSClassDeclaration>) {
        val resolved = type.resolve()
        val declaration = resolved.declaration
        // We really don't want to iterate over builtin types, and we need to be careful to only process everything once or this will be infinite recursion.
        if (!resolved.isBuiltinSerializableType() && declaration !in addTo && declaration is KSClassDeclaration) {
            addTo.add(declaration)
            for (arg in resolved.arguments) {
                addReferencedTypes(arg.nonNullType(), addTo)
            }
            // Include types referenced in properties of models as well
            for (property in declaration.getAllProperties()) {
                addReferencedTypes(property.type, addTo)
            }
        }
    }
}