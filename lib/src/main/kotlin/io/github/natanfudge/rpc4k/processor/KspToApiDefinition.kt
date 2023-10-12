package io.github.natanfudge.rpc4k.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.ksp.toTypeName
import io.github.natanfudge.rpc4k.processor.utils.getPublicApiFunctions
import io.github.natanfudge.rpc4k.processor.utils.nonNullQualifiedName
import io.github.natanfudge.rpc4k.processor.utils.nonNullReturnType
import io.github.natanfudge.rpc4k.processor.utils.nonNullType

object KspToApiDefinition {
    fun convert(kspClass: KSClassDeclaration): ApiDefinition {
        return ApiDefinition(
            name = kspClass.simpleName.getShortName(),
            methods = kspClass.getPublicApiFunctions().map { convertMethod(it) }.toList(),
        )
    }

    private fun convertMethod(kspMethod: KSFunctionDeclaration): RpcDefinition {
        return RpcDefinition(
            kspMethod.simpleName.getShortName(),
            args = kspMethod.parameters.map { convertArgument(it) }.toList(),
            returnType = convertType(kspMethod.nonNullReturnType())
        )
    }

    private fun convertArgument(argument: KSValueParameter): RpcArgumentDefinition {
        return RpcArgumentDefinition(
            name = argument.name?.getShortName() ?: error("Only named parameters are expected at the moment"),
            type = convertType(argument.type)
        )
    }

    private fun convertType(type: KSTypeReference): RpcType {
        val resolved = type.resolve()
        val declaration = resolved.declaration
        val qualifiedName = resolved.nonNullQualifiedName()
        val packageName = declaration.packageName.asString()
        val className = qualifiedName.removePrefix("$packageName.")

        return RpcType(
            packageName = packageName,
            simpleName = className,
            typeArguments = resolved.arguments.map { convertType(it.nonNullType()) },
            isNullable = resolved.isMarkedNullable
        )
    }
}