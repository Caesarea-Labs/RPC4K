package io.github.natanfudge.rpc4k.processor

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.symbol.ClassKind.*
import io.github.natanfudge.rpc4k.processor.utils.*

object KspToApiDefinition {
    fun toApiDefinition(kspClass: KSClassDeclaration): ApiDefinition {
        val (packageName, className) = kspClass.getPackageAndClassName()
        return ApiDefinition(
            // Doesn't quite fit, but good enough to represent the name as an KotlinTypeReference
            name = KotlinTypeReference(
                packageName = packageName, simpleName = className
            ),
            methods = kspClass.getPublicApiFunctions().map { toRpc(it) }.toList(),
            getRpcModels(kspClass)
        )
    }

    private fun toRpc(kspMethod: KSFunctionDeclaration): RpcDefinition {
        return RpcDefinition(
            kspMethod.simpleName.getShortName(),
            parameters = kspMethod.parameters.map { toRpcParameter(it) }.toList(),
            returnType = toKotlinTypeReference(kspMethod.nonNullReturnType())
        )
    }

    private fun toRpcParameter(argument: KSValueParameter): RpcParameter {
        return RpcParameter(
            name = argument.name?.getShortName() ?: error("Only named parameters are expected at the moment"),
            type = toKotlinTypeReference(argument.type)
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
        return kspClass.getReferencedClasses().map { toRpcModel(it) }
    }

    //TODO: I think we don't need this to return a list anymore since i simplified enum handling
    /**
     * This returns a list because sometimes two models spawn from one class declaration. Such a case is enums with values.
     */
    private fun toRpcModel(declaration: KSClassDeclaration): RpcModel = when (declaration.classKind) {
        INTERFACE -> toRpcUnionModel(declaration)
        // If it has sealed subclasses, it means it is a sealed type, and it should be treated as a union.
        CLASS, OBJECT -> if (declaration.getSealedSubclasses().count() == 0) toRpcStructModel(declaration) else toRpcUnionModel(declaration)
        ENUM_CLASS -> toRpcEnumModel(declaration)
        ENUM_ENTRY -> error("Enum entries can't be serializable")
        ANNOTATION_CLASS -> error("Annotation classes can't be serializable")
    }

    /**
     * Given a sealed class/interface
     * ```
     * @Serializable
     * sealed interface GenericSealed {
     *     @Serializable
     *     class GenericSubclass: GenericSealed
     * }
     * ```
     *
     * generates the [RpcModel.Union] for it.
     */
    private fun toRpcUnionModel(declaration: KSClassDeclaration) = RpcModel.Union(
            name = declaration.getSimpleName(),
            options = declaration.getSealedSubclasses().map { sealedSubclassToRpcType(it) }.toList(),
            declaration.typeParameters.map { it.name.asString() }
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
     *     Generic types could be implemented by adding inheritance to the format, but I don't think that's really required yet.
     *
     */
    private fun sealedSubclassToRpcType(sealedSubclass: KSClassDeclaration): KotlinTypeReference {
        val (packageName, className) = sealedSubclass.getPackageAndClassName()
        return KotlinTypeReference(packageName = packageName, simpleName = className)
    }

    /**
     * Converts an enum model like this:
     * ```
     * enum class MyClass(val x: Int, val y: Int)
     * ```
     * to a [RpcModel.Enum] (and sometimes also a [RpcModel.Struct])
     */
    private fun toRpcEnumModel(declaration: KSClassDeclaration): RpcModel {
//        val properties = declaration.getDeclaredProperties().toList()
        // Get all enum entries/options
        val options = declaration.declarations.filter { it is KSClassDeclaration && it.classKind == ENUM_ENTRY }
            .map { it.getSimpleName() }.toList()
        val name = declaration.getSimpleName()
        return RpcModel.Enum(name = name, options)
//        return if (properties.isEmpty()) {
//            // When it's a simple enum with no data, kotlin serializes it as a simple string union.
//
//        } else {
//            val model = toRpcStructModel(declaration)
//            // The "name" property is limited to only being one of the enum value's name, so we generate another model just for this property.
//            // The generated type will be called <enum-name>Options
//            val optionsEnum = RpcModel.Enum(name = declaration.simpleName.asString() + "Options", options)
//            // When it's an enum with data, it serializes it like a data class with the addition of the "name" property which has the
//            // enum value's name.
//            val nameType = KotlinTypeReference(
//                packageName = GeneratedModelsPackage, simpleName = optionsEnum.name
//            )
//            val enumStruct = model.copy(properties = model.properties + RpcModel.Struct.Property(name = "name", type = nameType))
//            listOf(enumStruct, optionsEnum)
//            //TODO: test on the other side we get a proper union enum as the type of the "name" property here
//        }
    }

    /**
     * Converts a model like this:
     * ```
     * data class MyClass(val x: Int, val y: Int)
     * ```
     * to a [RpcModel.Struct]
     */
    private fun toRpcStructModel(declaration: KSClassDeclaration): RpcModel.Struct {
        val properties = declaration.getDeclaredProperties().map {
            //TODO : KSP doesn't tell me if it has a default value... once again I need a compiler plugin for default stuff
            RpcModel.Struct.Property(name = it.getSimpleName(), type = toKotlinTypeReference(it.type), isOptional = false /*it.hasDefault*/)
        }.toList()

        return RpcModel.Struct(
            name = declaration.getSimpleName(),
            typeParameters = declaration.typeParameters.map { it.name.asString() },
            // If the struct is part of a tuple kotlinx.serialization also inserts a String "type" property
            properties = if (isPartOfTuple(declaration)) properties + (unionTypeDiscriminatorProperty) else properties,
        )
    }

    private val unionTypeDiscriminatorProperty = RpcModel.Struct.Property(
        ApiDefinitionConverters.UnionTypeDiscriminator, KotlinTypeReference.string
    )

    private fun isPartOfTuple(declaration: KSClassDeclaration) = declaration.getAllSuperTypes()
        .any { Modifier.SEALED in it.declaration.modifiers }

    /**
     * Get `com.foo.bar.MyClass<Int,String>` as an [KotlinTypeReference]
     * */
    private fun toKotlinTypeReference(type: KSTypeReference): KotlinTypeReference {
        val resolved = type.resolve()
        val declaration = resolved.declaration
        val (packageName, className) = declaration.getPackageAndClassName()
        val isTypeParameter = declaration is KSTypeParameter
        return KotlinTypeReference(
            packageName = packageName, simpleName = if (isTypeParameter) declaration.getSimpleName() else className,
            typeArguments = resolved.arguments.map { toKotlinTypeReference(it.nonNullType()) },
            isNullable = resolved.isMarkedNullable,
            isTypeParameter = isTypeParameter,
            inlinedType = declaration.inlinedType()
        )
    }

    private fun KSDeclaration.inlinedType(): KotlinTypeReference? {
        // Value classes may support multiple properties in the future so we require exactly one property to think ahead
        if (this !is KSClassDeclaration || Modifier.VALUE !in this.modifiers || this.getDeclaredProperties().count() != 1) return null
        return toKotlinTypeReference(getDeclaredProperties().single().type)
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
}

