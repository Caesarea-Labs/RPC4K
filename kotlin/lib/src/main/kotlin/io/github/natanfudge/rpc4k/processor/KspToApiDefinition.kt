package io.github.natanfudge.rpc4k.processor

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.symbol.ClassKind.*
import io.github.natanfudge.rpc4k.processor.utils.*

object KspToApiDefinition {
    fun toApiDefinition(kspClass: KSClassDeclaration): ApiDefinition {
        return ApiDefinition(
            // Doesn't quite fit, but good enough to represent the name as an KotlinTypeReference
            name = kspClass.getKotlinName(),
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
        // Sort to get deterministic results (this helps with incremental stuff)
        return kspClass.getReferencedClasses().sortedBy { it.nonNullQualifiedName() }.map { toRpcModel(it) }
    }

    /**
     * This returns a list because sometimes two models spawn from one class declaration. Such a case is enums with values.
     */
    private fun toRpcModel(declaration: KSClassDeclaration): RpcModel = when (declaration.classKind) {
        INTERFACE -> toRpcUnionModel(declaration)
        // If it has sealed subclasses, it means it is a sealed type, and it should be treated as a union.
        CLASS, OBJECT -> when {
            declaration.hasAnnotation(JvmInline::class) -> toRpcInlineModel(declaration)
            declaration.getSealedSubclasses().count() == 0 -> toRpcStructModel(declaration)
            else -> toRpcUnionModel(declaration)
        }

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
        return KotlinTypeReference(sealedSubclass.getKotlinName())
    }

    /**
     * Converts an enum model like this:
     * ```
     * enum class MyClass(val x: Int, val y: Int)
     * ```
     * to a [RpcModel.Enum] (and sometimes also a [RpcModel.Struct])
     */
    private fun toRpcEnumModel(declaration: KSClassDeclaration): RpcModel {
        // Get all enum entries/options
        val options = declaration.declarations.filter { it is KSClassDeclaration && it.classKind == ENUM_ENTRY }
            // Make sure to use the simple names for the enum entries
            .map { it.getTopLevelSimpleName() }.toList()
        val name = declaration.getSimpleName()
        return RpcModel.Enum(name = name, options)
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
            //NiceToHave: Support optional parameters and properties
            RpcModel.Struct.Property(name = it.getSimpleName(), type = toKotlinTypeReference(it.type), isOptional = false /*it.hasDefault*/)
        }.toList()

        val name = declaration.getKotlinName()

        return RpcModel.Struct(
            name = name.simple,
            typeParameters = declaration.typeParameters.map { it.name.asString() },
            hasTypeDiscriminator = isPartOfTuple(declaration),
            // If the struct is part of a tuple kotlinx.serialization also inserts a String "type" property
            properties = properties,
            packageName = name.pkg
        )
    }

    private fun toRpcInlineModel(declaration: KSClassDeclaration): RpcModel.Inline {
        return RpcModel.Inline(
            name = declaration.getSimpleName(),
            typeParameters = declaration.typeParameters.map { it.name.asString() },
            inlinedType = toKotlinTypeReference(declaration.getDeclaredProperties().single().type)
        )
    }

//    private val unionTypeDiscriminatorProperty = RpcModel.Struct.Property(
//        ApiDefinitionConverters.UnionTypeDiscriminator, KotlinTypeReference.string
//    )

    private fun isPartOfTuple(declaration: KSClassDeclaration) = declaration.getAllSuperTypes()
        .any { Modifier.SEALED in it.declaration.modifiers }

    /**
     * Get `com.foo.bar.MyClass<Int,String>` as an [KotlinTypeReference]
     * */
    private fun toKotlinTypeReference(type: KSTypeReference): KotlinTypeReference {
        val resolved = type.resolveToUnderlying()
        val declaration = resolved.declaration
        val kotlinName = declaration.getKotlinName()
        val isTypeParameter = declaration is KSTypeParameter
        return KotlinTypeReference(
            kotlinName.copy(simple = if (isTypeParameter) declaration.getSimpleName() else kotlinName.simple),
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


}

