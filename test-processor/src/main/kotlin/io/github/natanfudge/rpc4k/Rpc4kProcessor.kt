@file:JvmName("AsdfKt")

package io.github.natanfudge.rpc4k

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.OutputStreamWriter
import kotlin.system.measureTimeMillis

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Api

@PublishedApi
internal const val GeneratedClientImplSuffix = "ClientImpl"

@PublishedApi
internal const val GeneratedServerImplSuffix = "Decoder"


class Rpc4kProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> = invokeOnce(orElse = { emptyList() }) {
        resolver.getSymbolsWithAnnotation("io.github.natanfudge.rpc4k.Api")
            .filter { it.validate() }
            .filterIsInstance<KSClassDeclaration>()
            .forEach { generateRpc(it) }

        listOf()
    }

    private fun generateRpc(apiClass: KSClassDeclaration) {
        val time = measureTimeMillis {
            generateClientImplementation(apiClass)
            generateServerDecoder(apiClass)
        }

        env.logger.warn("Generated RPC classes for: ${apiClass.qualifiedName!!.asString()} in $time millis")
    }

    private fun generateClientImplementation(apiClass: KSClassDeclaration) {
        val generatedClassName = apiClass.simpleName.asString() + GeneratedClientImplSuffix
        apiClass.createKtFile(apiClass.packageName.asString(), generatedClassName) {
            addImport("kotlinx.serialization.builtins", "serializer")

            addType(
                TypeSpec.classBuilder(generatedClassName)
                    .constructorProperty(
                        name = "client",
                        type = RpcClient::class.asTypeName(),
                        KModifier.PRIVATE
                    )
                    .superclass(apiClass.qualifiedName!!.toTypeName())
                    .apply {
                        apiClass.getActualFunctions().forEach { generateClientMethodImplementation(it) }
                    }
                    .build()
            )
        }
    }

    private fun TypeSpec.Builder.generateClientMethodImplementation(apiMethod: KSFunctionDeclaration) {
        val methodName = apiMethod.simpleName.asString()

        val returnTypeName = apiMethod.returnType!!.toTypeName()

        addFunction(FunSpec.builder(methodName)
            .addModifiers(KModifier.OVERRIDE)
            .generateClientMethodImplementationParameters(apiMethod)
            .apply {
                val parameterSerializersString = apiMethod.parameters
                    .joinToString(",\n") { "${it.name!!.asString()} to %T.serializer()" }

                val types = listOf(
                    Rpc4KGeneratedClientUtils::class.asTypeName()
                ) + apiMethod.parameters.map { it.type.toTypeName() } + returnTypeName

                addStatement(
                    """
                    |return %T.send(
                    |       client,
                    |       "$methodName",
                    |       listOf(
                    |${"          $parameterSerializersString".indentAllNewLines()}
                    |       ),
                    |       %T.serializer()
                    |    )
                        """.trimMargin(),
                    *types.toTypedArray()
                )
            }
            .returns(returnTypeName)
            .build()
        )
    }


    private fun FunSpec.Builder.generateClientMethodImplementationParameters(apiMethod: KSFunctionDeclaration): FunSpec.Builder {
        for (parameter in apiMethod.parameters) {
            addParameter(
                name = parameter.name!!.asString(),
                type = parameter.type.toTypeName()
            )
        }
        return this
    }

    private fun generateServerDecoder(apiClass: KSClassDeclaration) {
        val generatedClassName = apiClass.simpleName.asString() + GeneratedServerImplSuffix
        val apiClassTypeName = apiClass.qualifiedName!!.toTypeName()
        apiClass.createKtFile(apiClass.packageName.asString(), generatedClassName) {
            addImport("kotlinx.serialization.builtins", "serializer")

            addType(
                TypeSpec.classBuilder(generatedClassName)
                    .constructorProperty(
                        name = "protocol",
                        type = apiClassTypeName,
                        KModifier.PRIVATE
                    )
                    .addSuperinterface(ProtocolDecoder::class.asTypeName().parameterizedBy(apiClassTypeName))
                    .generateServerMethodImplementation(apiClass)
                    .build()
            )
        }
    }

    private fun TypeSpec.Builder.generateServerMethodImplementation(apiClass: KSClassDeclaration): TypeSpec.Builder {
        addFunction(FunSpec.builder("accept")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter(name = "route", type = String::class)
            .addParameter(name = "args", type = List::class.parameterizedBy(String::class))
            .returns(String::class)
            .apply {
                val routesFormatString = apiClass.getActualFunctions()
                    .map { generateServerRouteDecoder(it) }.toList().join("\n")


                val finalFormatString =  """
                    |return when (route) {
                    |    %FS
                    |    else -> %T.invalidRoute(route)
                    |}
                """.trimMargin()
                    .formatType(Rpc4kGeneratedServerUtils::class.asTypeName())
                    .formatWith(routesFormatString)

                addStatement(finalFormatString)
            }
            .build()
        )
        return this
    }

    private fun generateServerRouteDecoder(route: KSFunctionDeclaration): FormattedString {
        val methodName = route.simpleName.asString()
        val parameters = route.parameters.mapIndexed(::generateServerParameterDecoder).join(",\n")
        return """
            |"$methodName" -> %T.encodeResponse(
            |    %T.serializer(), protocol.$methodName(
            |        %FS
            |    )
            |)
        """.trimMargin()
            .formatType(Rpc4kGeneratedServerUtils::class.asTypeName(), route.returnType!!.toTypeName())
            .formatWith(parameters)
    }

    private fun generateServerParameterDecoder(index: Int, parameter: KSValueParameter): FormattedString {
        return """
            %T.decodeParameter(%T.serializer(), args[$index])
        """.trimIndent().formatType(Rpc4kGeneratedServerUtils::class.asTypeName(), parameter.type.toTypeName())
    }


    private fun TypeSpec.Builder.constructorProperty(
        name: String,
        type: TypeName,
        vararg modifiers: KModifier
    ): TypeSpec.Builder {
        primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter(name, type)
                .build()
        )

        addProperty(PropertySpec.builder(name, type).initializer(name).addModifiers(*modifiers).build())
        return this
    }

    private fun KSClassDeclaration.createKtFile(
        packageName: String,
        className: String,
        builder: FileSpec.Builder.() -> Unit
    ) {
        val fileOutputStream = env.codeGenerator.createNewFile(
            dependencies = Dependencies(false, this.containingFile!!),
            packageName = packageName,
            fileName = className,
            extensionName = "kt"
        )

        val ktFile = FileSpec.builder(packageName, className).apply(builder).build()

        val writer = OutputStreamWriter(fileOutputStream)
        writer.use(ktFile::writeTo)
    }

    private var invoked = false

    private fun <T> invokeOnce(orElse: () -> T, invocation: () -> T): T {
        return if (!invoked) {
            invoked = true
            invocation()
        } else orElse()
    }


}

class Rpc4kProcessorProvider : SymbolProcessorProvider {
    override fun create(
        env: SymbolProcessorEnvironment
    ): SymbolProcessor {
        return Rpc4kProcessor(env)
    }
}
