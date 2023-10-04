package io.github.natanfudge.rpc4k.processor

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import io.github.natanfudge.rpc4k.runtime.impl.DecoderContext
import io.github.natanfudge.rpc4k.runtime.impl.Rpc4kGeneratedServerUtils
import io.github.natanfudge.rpc4k.runtime.api.server.ProtocolDecoder
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.KSerializer

internal object ServerDecoderGenerator {
     fun generate(env : SymbolProcessorEnvironment, apiClass: KSClassDeclaration) {
        val generatedClassName = apiClass.simpleName.asString() + GeneratedServerImplSuffix
        val apiClassTypeName = apiClass.qualifiedName!!.toTypeName()
        apiClass.createKtFile(env, apiClass.packageName.asString(), generatedClassName) {
            importClass(Rpc4kGeneratedServerUtils::class)
            importClass(KSerializer::class)
            importClass(Flow::class)

            importBuiltinSerializers()

            addClass(generatedClassName) {
                primaryConstructor {
                    constructorProperty(
                        name = "protocol",
                        type = apiClassTypeName,
                        KModifier.PRIVATE
                    )
                    constructorProperty(
                        name = "context",
                        type = DecoderContext::class.asTypeName(),
                        KModifier.PRIVATE
                    )
                }
                addSuperinterface(
                    ProtocolDecoder::class.asTypeName().parameterizedBy(apiClassTypeName)
                )
                generateServerMethodImplementation(apiClass)
            }
        }
    }

    private fun TypeSpec.Builder.generateServerMethodImplementation(apiClass: KSClassDeclaration): TypeSpec.Builder {
        addFunction("accept") {
            addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
            addParameter(name = "route", type = String::class)
            addParameter(name = "args", type = List::class.parameterizedBy(ByteArray::class))
            returns(Any::class)
            val routesFormatString = apiClass.getPublicApiFunctions()
                .map { generateServerRouteDecoder(it) }.toList().join("\n")

            // p and r are shorthand methods
            val finalFormatString = """
        fun <T> p(serializer: KSerializer<T>, index: Int) =
            Rpc4kGeneratedServerUtils.decodeParameter(context, serializer, args, index)

        fun <T> r(serializer: KSerializer<T>, value: T) =
            Rpc4kGeneratedServerUtils.encodeResponse(context.format, serializer, value)

        fun <T> r(serializer: KSerializer<T>, value: Flow<T>) =
            Rpc4kGeneratedServerUtils.encodeFlowResponse(context.format, serializer, value)
                    |return when (route) {
                    |    %FS
                    |    else -> %T.invalidRoute(route)
                    |}
                """.trimMargin()
                .formatType(Rpc4kGeneratedServerUtils::class.asTypeName())
                .formatWith(routesFormatString)

            addStatement(finalFormatString)

        }
        return this
    }

    private fun generateServerRouteDecoder(route: KSFunctionDeclaration): FormattedString {
        val methodName = route.simpleName.asString()
        val parameters = route.parameters.mapIndexed(ServerDecoderGenerator::generateServerParameterDecoder).join(",\n")

        val returnType = route.returnType!!
        return """
            |"$methodName" -> r(%FS, protocol.$methodName(
            |        %FS
            |    )
            |)
        """.trimMargin()
            .formatType(Rpc4kGeneratedServerUtils::class.asTypeName())
            .formatWith(returnType.returnTypeSerializer(), parameters)
    }

    private fun generateServerParameterDecoder(
        index: Int,
        parameter: KSValueParameter
    ): FormattedString {
        return """
            p(%FS, $index)
        """.trimIndent()
            .formatType(Rpc4kGeneratedServerUtils::class.asTypeName())
            .formatWith(parameter.type.serializerString())
    }
}