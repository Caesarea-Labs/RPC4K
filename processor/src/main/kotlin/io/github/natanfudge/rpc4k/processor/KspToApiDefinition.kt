package io.github.natanfudge.rpc4k.processor

import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import io.github.natanfudge.rpc4k.processor.old.toTypeName
import io.github.natanfudge.rpc4k.processor.utils.getPublicApiFunctions
import io.github.natanfudge.rpc4k.processor.utils.nonNullReturnType

object KspToApiDefinition {
    fun convert(kspClass: KSClassDeclaration): ApiDefinition {
        return ApiDefinition(
            name = kspClass.simpleName.getShortName(),
            implementationPackageName = kspClass.packageName.asString(),
            methods = kspClass.getPublicApiFunctions().map { convertMethod(it) }.toList(),
            isInterface = kspClass.classKind == ClassKind.INTERFACE
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
        return RpcType.Ksp(type)
    }
}