package io.github.natanfudge.rpc4k.processor.old

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import io.github.natanfudge.rpc4k.runtime.impl.Rpc4KGeneratedClientUtils
import io.github.natanfudge.rpc4k.runtime.impl.RpcClientComponents

internal object ClientImplGenerator {
     fun generate(env: SymbolProcessorEnvironment, apiClass: KSClassDeclaration) {
        val generatedClassName = apiClass.simpleName.asString() + GeneratedClientImplSuffix
        apiClass.createKtFile(env, apiClass.packageName.asString(), generatedClassName) {
            importBuiltinSerializers()
            addImport(Rpc4KGeneratedClientUtils::class, "send")
            addImport(Rpc4KGeneratedClientUtils::class, "sendFlow")


            addClass(generatedClassName) {
                primaryConstructor {
                    constructorProperty(
                        name = "client",
                        type = RpcClientComponents::class.asTypeName(),
                        KModifier.PRIVATE
                    )
                }
                superclass(apiClass.qualifiedName!!.toTypeName())
                apiClass.getPublicApiFunctions().forEach { addClientMethodImplementation(it) }
            }
        }
    }

    private fun TypeSpec.Builder.addClientMethodImplementation(apiMethod: KSFunctionDeclaration) {
        val methodName = apiMethod.simpleName.asString()

        val returnType = apiMethod.returnType
        val returnTypeName = returnType!!.toTypeName()

        addFunction(methodName) {
            addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
            addClientMethodImplementationParameters(apiMethod)
            returns(returnTypeName)

            val parameterSerializers = apiMethod.parameters
                .map {
                    "${it.name!!.asString()} to %FS".format.formatWith(
                        it.type.serializerString()
                    )
                }
                .join(",\n")

            val implementation = """
                    |return %T.${if (returnType.isFlow()) "sendFlow" else "send"}(
                    |       this.client,
                    |       "$methodName",
                    |       %FS,
                    |       %FS
                    |    )
                        """.trimMargin()
                .formatType(Rpc4KGeneratedClientUtils::class.asTypeName())
                .formatWith(returnType.returnTypeSerializer(), parameterSerializers)

            addStatement(implementation)
        }
    }

    private fun FunSpec.Builder.addClientMethodImplementationParameters(apiMethod: KSFunctionDeclaration) {
        for (parameter in apiMethod.parameters) {
            addParameter(
                name = parameter.name!!.asString(),
                type = parameter.type.toTypeName()
            )
        }
    }
}