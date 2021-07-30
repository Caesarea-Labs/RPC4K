package io.github.natanfudge.rpc4k.impl

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.natanfudge.rpc4k.Api
import io.github.natanfudge.rpc4k.ProtocolDecoder
import io.github.natanfudge.rpc4k.RpcClient
import io.github.natanfudge.rpc4k.SerializationFormat
import io.github.natanfudge.rpc4k.impl.Rpc4kGeneratedServerUtils.encodeResponse
import java.io.OutputStreamWriter
import kotlin.system.measureTimeMillis


@PublishedApi
internal const val GeneratedClientImplSuffix = "ClientImpl"

@PublishedApi
internal const val GeneratedServerImplSuffix = "Decoder"


internal class Rpc4kProcessorProvider : SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment
    ): SymbolProcessor = Rpc4kProcessor(environment)
}

internal class UnsupportedApiException(message: String) : Exception(message)

fun fib(num: Int): Int {
    if (num == 1) return 1
    if (num == 0) return 0
    else return fib(num - 1) + fib(num - 2)
}

internal class Rpc4kProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> = invokeOnce(orElse = { emptyList() }) {
//        fib(42)
        val symbols =  resolver.getSymbolsWithAnnotation(Api::class.qualifiedName!!).toList()
        println("Symbols: $symbols")
        resolver.getSymbolsWithAnnotation(Api::class.qualifiedName!!)
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
//        env.logger.error("Asdfasdf")
        println("Generated RPC classes for: ${apiClass.qualifiedName!!.asString()} in $time millis")
    }

    private fun generateClientImplementation(apiClass: KSClassDeclaration) {
        val generatedClassName = apiClass.simpleName.asString() + GeneratedClientImplSuffix
        apiClass.createKtFile(apiClass.packageName.asString(), generatedClassName) {
            importBuiltinSerializers()
            addClass(generatedClassName) {
                primaryConstructor {
                    constructorProperty(
                        name = "client",
                        type = RpcClient::class.asTypeName(),
                        KModifier.PRIVATE
                    )
                }
                superclass(apiClass.qualifiedName!!.toTypeName())
                apiClass.getActualFunctions().forEach { addClientMethodImplementation(it) }
            }
        }
    }

    private fun ParameterizedTypeName.specialSerializer(name: String, typeArgumentAmount: Int): FormattedString {
        assert(typeArguments.size == typeArgumentAmount)

        return "$name(%FS)".formatWith(
            List(typeArgumentAmount) { typeArguments[it].serializerString() }.join(", ")
        )
    }

    private fun TypeName.serializerString(): FormattedString {
        val string = if (this is ParameterizedTypeName) {
            when (this.rawType.canonicalName) {
                List::class.qualifiedName -> specialSerializer("ListSerializer", 1)
                Set::class.qualifiedName -> specialSerializer("SetSerializer", 1)
                Map::class.qualifiedName -> specialSerializer("MapSerializer", 2)
                Pair::class.qualifiedName -> specialSerializer("PairSerializer", 2)
                Map.Entry::class.qualifiedName -> specialSerializer("MapEntrySerializer", 2)
                Triple::class.qualifiedName -> specialSerializer("TripleSerializer", 3)
                else -> throw UnsupportedApiException("Generic types other than List, Set and Map are not supported.")
            }
        } else {
            "%T.serializer()".formatType(this.copy(nullable = false))
        }
        return if (isNullable) "%FS.nullable".formatWith(string)
        else string
    }

    private fun TypeSpec.Builder.addClientMethodImplementation(apiMethod: KSFunctionDeclaration) {
        val methodName = apiMethod.simpleName.asString()

        val returnTypeName = apiMethod.returnType!!.toTypeName()

        addFunction(methodName) {
            addModifiers(KModifier.OVERRIDE)
            addClientMethodImplementationParameters(apiMethod)
            returns(returnTypeName)

            val parameterSerializers = apiMethod.parameters
                .map { "${it.name!!.asString()} to %FS".format.formatWith(it.type.toTypeName().serializerString()) }
                .join(",\n")

            val implementation = """
                    |return %T.send(
                    |       this.client,
                    |       "$methodName",
                    |       listOf(
                    |           %FS
                    |       ),
                    |       %FS
                    |    )
                        """.trimMargin()
                .formatType(Rpc4KGeneratedClientUtils::class.asTypeName())
                .formatWith(parameterSerializers, returnTypeName.serializerString())

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

    private fun generateServerDecoder(apiClass: KSClassDeclaration) {
        val generatedClassName = apiClass.simpleName.asString() + GeneratedServerImplSuffix
        val apiClassTypeName = apiClass.qualifiedName!!.toTypeName()
        apiClass.createKtFile(apiClass.packageName.asString(), generatedClassName) {
            addImport(  "io.github.natanfudge.rpc4k.impl","Rpc4kGeneratedServerUtils.encodeResponse","Rpc4kGeneratedServerUtils.decodeParameter")

            importBuiltinSerializers()

            addClass(generatedClassName) {
                primaryConstructor {
                    constructorProperty(
                        name = "protocol",
                        type = apiClassTypeName,
                        KModifier.PRIVATE
                    )
                    constructorProperty(
                        name = "format",
                        type = SerializationFormat::class.asTypeName(),
                        KModifier.PRIVATE
                    )
                }
                addSuperinterface(ProtocolDecoder::class.asTypeName().parameterizedBy(apiClassTypeName))
                generateServerMethodImplementation(apiClass)
            }
        }
    }


    private fun TypeSpec.Builder.generateServerMethodImplementation(apiClass: KSClassDeclaration): TypeSpec.Builder {
        addFunction("accept") {
            addModifiers(KModifier.OVERRIDE)
            addParameter(name = "route", type = String::class)
            addParameter(name = "args", type = List::class.parameterizedBy(ByteArray::class))
            returns(ByteArray::class)
            val routesFormatString = apiClass.getActualFunctions()
                .map { generateServerRouteDecoder(it) }.toList().join("\n")

            val finalFormatString = """
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
        val parameters = route.parameters.mapIndexed(::generateServerParameterDecoder).join(",\n")
        return """
            |"$methodName" -> %T.encodeResponse(
            |    this.format, %FS, this.protocol.$methodName(
            |        %FS
            |    )
            |)
        """.trimMargin()
            .formatType(Rpc4kGeneratedServerUtils::class.asTypeName())
            .formatWith(route.returnType!!.toTypeName().serializerString(), parameters)
    }

    private fun generateServerParameterDecoder(index: Int, parameter: KSValueParameter): FormattedString {
        return """
            %T.decodeParameter(this.format, %FS, args[$index])
        """.trimIndent()
            .formatType(Rpc4kGeneratedServerUtils::class.asTypeName())
            .formatWith(parameter.type.toTypeName().serializerString())
    }

    private fun FileSpec.Builder.importBuiltinSerializers() {
        addImport("kotlinx.serialization.builtins",
            "serializer","nullable","ListSerializer","SetSerializer","MapSerializer","PairSerializer","MapEntrySerializer","TripleSerializer")

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

