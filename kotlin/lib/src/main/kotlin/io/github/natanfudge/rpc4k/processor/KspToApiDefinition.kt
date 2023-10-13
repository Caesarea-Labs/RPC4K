package io.github.natanfudge.rpc4k.processor

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.symbol.ClassKind.*
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
     * MyClass(val x: Int, val y: Int)
     * ```
     * as [RpcModel]s
     */
    private fun getRpcModels(kspClass: KSClassDeclaration): List<RpcModel> {
        return getReferencedTypes(kspClass).flatMap { toRpcModels(it) }
    }

    /**
     * This returns a list because sometimes two models spawn from one class declaration. Such a case is enums with values.
     */
    private fun toRpcModels(declaration: KSClassDeclaration): List<RpcModel> = when (declaration.classKind) {
        INTERFACE -> toRpcUnionModel(declaration)
        // If it has sealed subclasses, it means it is a sealed type, and it should be treated as a union.
        CLASS, OBJECT -> if (declaration.getSealedSubclasses().count() == 0) listOf(toRpcStructModel(declaration)) else toRpcUnionModel(declaration)
        ENUM_CLASS -> toRpcEnumModel(declaration)
        ENUM_ENTRY -> error("Enum entries can't be serializable")
        ANNOTATION_CLASS -> error("Annotation classes can't be serializable")
    }

    private fun toRpcUnionModel(declaration: KSClassDeclaration) = listOf(
        RpcModel.Union(name = declaration.getSimpleName(), options = declaration.getSealedSubclasses().map { it.getSimpleName() }.toList())
    )

    /**
     * Converts an enum model like this:
     * ```
     * enum class MyClass(val x: Int, val y: Int)
     * ```
     * to a [RpcModel.Enum] (and sometimes also a [RpcModel.Struct])
     */
    private fun toRpcEnumModel(declaration: KSClassDeclaration): List<RpcModel> {
        val properties = declaration.getDeclaredProperties().toList()
        // Get all enum entries/options
        val options = declaration.declarations.filter { it is KSClassDeclaration && it.classKind == ENUM_ENTRY }
            .map { it.getSimpleName() }.toList()
        val name = declaration.getSimpleName()
        return if (properties.isEmpty()) {
            // When it's a simple enum with no data, kotlin serializes it as a simple string union.
            listOf(RpcModel.Enum(name = name, options))
        } else {
            val model = toRpcStructModel(declaration)
            // The "name" property is limited to only being one of the enum value's name, so we generate another model just for this property.
            // The generated type will be called <enum-name>Options
            val optionsEnum = RpcModel.Enum(name = declaration.simpleName.asString() + "Options", options)
            // When it's an enum with data, it serializes it like a data class with the addition of the "name" property which has the
            // enum value's name.
            val nameType = RpcType(name = optionsEnum.name, isTypeParameter = false, isOptional = false, typeArguments = listOf())
            val enumStruct = model.copy(properties = model.properties + ("name" to nameType))
            listOf(enumStruct, optionsEnum)
            //TODO: test on the other side we get a proper union enum as the type of the "name" property here
        }
    }

    /**
     * Converts a model like this:
     * ```
     * data class MyClass(val x: Int, val y: Int)
     * ```
     * to a [RpcModel.Struct]
     */
    private fun toRpcStructModel(declaration: KSClassDeclaration) = RpcModel.Struct(
        name = declaration.getSimpleName(),
        typeParameters = declaration.typeParameters.map { it.name.asString() },
        properties = declaration.getDeclaredProperties().map {
            it.getSimpleName() to toRpcType(it.type)
        }.toMap()
    )

    /**
     * Get `MyClass<Int,T>` as an [RpcType]
     * */
    private fun toRpcType(reference: KSTypeReference): RpcType {
        val type = reference.resolve()
        val declaration = type.declaration
        val (_, className) = declaration.getPackageAndClassName()
        return RpcType(
            name = if (declaration is KSTypeParameter) declaration.name.getShortName() else className,
            isOptional = type.isMarkedNullable,
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
        // Add things referenced in methods
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
            for (arg in resolved.arguments) {
                addReferencedTypes(arg.nonNullType(), addTo)
            }
            addReferencedTypes(declaration, addTo)
        }
    }

    private fun addReferencedTypes(declaration: KSClassDeclaration, addTo: MutableSet<KSClassDeclaration>) {
        addTo.add(declaration)
        // Include types referenced in properties of models as well
        for (property in declaration.getAllProperties()) {
            addReferencedTypes(property.type, addTo)
        }
        // Add sealed subclasses as well
        for(sealedSubClass in declaration.getSealedSubclasses()) {
            addReferencedTypes(sealedSubClass, addTo)
        }
    }
}