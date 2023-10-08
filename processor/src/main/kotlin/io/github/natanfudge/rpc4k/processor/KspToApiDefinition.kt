package io.github.natanfudge.rpc4k.processor

import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import io.github.natanfudge.rpc4k.processor.old.toTypeName

object KspToApiDefinition {
    fun convert(kspClass: KSClassDeclaration): ApiDefinition {
        return ApiDefinition(
            name = kspClass.simpleName.getShortName(),
            methods = kspClass.getAllFunctions().map { convertMethod(it) }.toList()
        )
    }

    private fun convertMethod(kspMethod: KSFunctionDeclaration): RpcDefinition {
        return RpcDefinition(
            kspMethod.simpleName.getShortName(),
            args = kspMethod.parameters.map { convertArgument(it) }.toList(),
            returnType = convertType(kspMethod.returnType)
        )
    }

    private fun convertArgument(argument: KSValueParameter): RpcArgumentDefinition {
        return RpcArgumentDefinition(
            name = argument.name?.getShortName() ?: error("Only named parameters are expected at the moment"),
            type = convertType(argument.type)
        )
    }

    private fun convertType(type: KSTypeReference?): RpcType {
        if (type == null) return Unit::class.java.asTypeName()
        return type.resolve().toTypeName()
    }
}