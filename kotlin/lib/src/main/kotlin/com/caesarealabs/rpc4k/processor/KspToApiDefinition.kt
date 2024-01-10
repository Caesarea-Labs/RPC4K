package com.caesarealabs.rpc4k.processor

import com.caesarealabs.rpc4k.processor.utils.*
import com.caesarealabs.rpc4k.runtime.api.Dispatch
import com.caesarealabs.rpc4k.runtime.api.RpcEvent
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.symbol.ClassKind.*

internal class KspToApiDefinition(private val resolver: Resolver) {
    fun toApiDefinition(kspClass: KSClassDeclaration): RpcApi? {
        val functions = kspClass.getPublicApiFunctions()
        val endpoints = functions.map { toRpc(it) }.toList().allOrNothing() ?: return null

        return RpcApi(
            // Doesn't quite fit, but good enough to represent the name as an KotlinTypeReference
            name = kspClass.getKotlinName() ?: return null,
            methods = endpoints.filterIsInstance<RpcFunction>(),
            models = getRpcModels(kspClass) ?: return null,
            events = endpoints.filterIsInstance<RpcEventEndpoint>()
        )
    }
    @OptIn(KspExperimental::class)
    private fun toRpc(kspMethod: KSFunctionDeclaration): RpcEndpoint? {
        val name = kspMethod.simpleName.getShortName()
        val returnType = toKotlinTypeReference(kspMethod.nonNullReturnType()) ?: return null
        if (kspMethod.isAnnotationPresent(RpcEvent::class)) {
            val parameters = getRpcEventParameters(kspMethod) ?: return null
            return RpcEventEndpoint(name, parameters, returnType)
        } else {
            val parameters = kspMethod.parameters.map { toRpcParameter(it) }.toList().allOrNothing() ?: return null
            return RpcFunction(name, parameters, returnType)
        }

    }

    @OptIn(KspExperimental::class)
    private fun getRpcEventParameters(kspMethod: KSFunctionDeclaration) = kspMethod.parameters.map {
        val param = toRpcParameter(it)
        EventParameter(
            isDispatch = it.isAnnotationPresent(Dispatch::class),
            param ?: return null
        )
    }.toList().allOrNothing()


    private fun toRpcParameter(parameter: KSValueParameter): RpcParameter? {
        return RpcParameter(
            name = parameter.name?.getShortName() ?: error("Only named parameters are expected at the moment"),
            type = toKotlinTypeReference(parameter.type) ?: return null,
        )
    }


    /**
     * Get list of models like this:
     * ```
     * MyClass(val x: Int, val y: Int)
     * ```
     * as [RpcModel]s
     */
    private fun getRpcModels(kspClass: KSClassDeclaration): List<RpcModel>? {
        // Sort to get deterministic results (this helps with incremental stuff)
        return kspClass.getReferencedClasses(resolver).sortedBy { it.getQualifiedName() }.map { toRpcModel(it) }
            .allOrNothing()
    }

    /**
     * This returns a list because sometimes two models spawn from one class declaration. Such a case is enums with values.
     */
    private fun toRpcModel(declaration: KSClassDeclaration): RpcModel? = when (declaration.classKind) {
        INTERFACE -> toRpcUnionModel(declaration)
        // If it has sealed subclasses, it means it is a sealed type, and it should be treated as a union.
        CLASS, OBJECT -> when {
            declaration.hasAnnotation(JvmInline::class) -> toRpcInlineModel(declaration)
            declaration.fastGetSealedSubclasses(resolver).count() == 0 -> toRpcStructModel(declaration)
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
    private fun toRpcUnionModel(declaration: KSClassDeclaration): RpcModel.Union? {
        return RpcModel.Union(
            name = declaration.getSimpleName() ?: return null,
            options = declaration.fastGetSealedSubclasses(resolver).map { sealedSubclassToRpcType(it) }.toList().allOrNothing() ?: return null,
            declaration.typeParameters.map { it.name.asString() }
        )
    }


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
    private fun sealedSubclassToRpcType(sealedSubclass: KSClassDeclaration): KotlinTypeReference? {
        return KotlinTypeReference(sealedSubclass.getKotlinName() ?: return null)
    }

    /**
     * Converts an enum model like this:
     * ```
     * enum class MyClass(val x: Int, val y: Int)
     * ```
     * to a [RpcModel.Enum] (and sometimes also a [RpcModel.Struct])
     */
    private fun toRpcEnumModel(declaration: KSClassDeclaration): RpcModel? {
        // Get all enum entries/options
        val options = declaration.declarations.filter { it is KSClassDeclaration && it.classKind == ENUM_ENTRY }
            // Make sure to use the simple names for the enum entries
            .map { it.getTopLevelSimpleName() }.toList()
        val name = declaration.getSimpleName()
        return RpcModel.Enum(name = name ?: return null, options)
    }

    /**
     * Converts a model like this:
     * ```
     * data class MyClass(val x: Int, val y: Int)
     * ```
     * to a [RpcModel.Struct]
     */
    private fun toRpcStructModel(declaration: KSClassDeclaration): RpcModel.Struct? {
        val properties = declaration.getDeclaredProperties().map {
            //NiceToHave: Support optional parameters and properties
            RpcModel.Struct.Property(name = it.getSimpleName() ?: return@map null, type = toKotlinTypeReference(it.type) ?: return@map null)
        }.toList().allOrNothing() ?: return null

        val name = declaration.getKotlinName()

        return RpcModel.Struct(
            name = name?.simple ?: return null,
            typeParameters = declaration.typeParameters.map { it.name.asString() },
            hasTypeDiscriminator = isPartOfTuple(declaration),
            // If the struct is part of a tuple kotlinx.serialization also inserts a String "type" property
            properties = properties,
            packageName = name.pkg
        )
    }

    private fun toRpcInlineModel(declaration: KSClassDeclaration): RpcModel.Inline? {
        return RpcModel.Inline(
            name = declaration.getSimpleName() ?: return null,
            typeParameters = declaration.typeParameters.map { it.name.asString() },
            inlinedType = toKotlinTypeReference(declaration.getDeclaredProperties().single().type) ?: return null
        )
    }

//    private val unionTypeDiscriminatorProperty = RpcModel.Struct.Property(
//        ApiDefinitionConverters.UnionTypeDiscriminator, KotlinTypeReference.string
//    )

    private fun isPartOfTuple(declaration: KSClassDeclaration) = declaration.getAllSuperTypes()
        .any { Modifier.SEALED in it.declaration.modifiers }

    /**
     * Get `com.foo.bar.MyClass<Int,String>` as an [KotlinTypeReference]
     * @param typeParameterResolver If specified, type parameters should be transformed to the given KSType of the name resolves to a
     * non-null KSType.
     * */
    private fun toKotlinTypeReference(type: KSTypeReference, typeParameterResolver: ((String) -> KSTypeReference?)? = null): KotlinTypeReference? {
        val resolved = type.resolveToUnderlying()
        val declaration = resolved.declaration
        val kotlinName = declaration.getKotlinName()
        val isTypeParameter = declaration is KSTypeParameter
        if (isTypeParameter) {
            // Resolve type parameters if they can be resolved
            val resolvedType = typeParameterResolver?.invoke(declaration.getSimpleName() ?: return null)
            if (resolvedType != null) return toKotlinTypeReference(resolvedType, typeParameterResolver)
        }
        return KotlinTypeReference(
            kotlinName?.copy(simple = (if (isTypeParameter) declaration.getSimpleName() else kotlinName.simple) ?: return null) ?: return null,
            typeArguments = resolved.arguments.map { toKotlinTypeReference(it.nonNullType(), typeParameterResolver) }.allOrNothing() ?: return null,
            isNullable = resolved.isMarkedNullable,
            isTypeParameter = isTypeParameter,
            inlinedType = resolved.inlinedType()
        )
    }

    // fun foo(

    // reference: GenericInline<K>

    // GenericInline declaration: GenericInline<T>(val value: T)

    private fun KSType.inlinedType(): KotlinTypeReference? {
        val decl = declaration
        // Value classes may support multiple properties in the future so we require exactly one property to think ahead
        if (decl !is KSClassDeclaration || Modifier.VALUE !in decl.modifiers || decl.getDeclaredProperties().count() != 1) return null
        val typeParameters = decl.typeParameters.map { it.name.asString() }
        val typeArguments = arguments.map { it.nonNullType() }
        val parameterToType = typeParameters.zip(typeArguments).toMap()
        // Provide the actual type argument to the type parameters of the inlined type
        return toKotlinTypeReference(decl.getDeclaredProperties().single().type) { parameterToType[it] }
    }


//NiceToHave: Respect @SerialName
//    /** Use the class's name normally, but respect `@SerialName` */
//    private fun KSDeclaration.getFinalName(): String {
//        val serialNameAnnotation = annotations.find { it.shortName.asString() == serialNameClass } ?: return getSimpleName()
//        return serialNameAnnotation.arguments[0].value as String
//    }

}

