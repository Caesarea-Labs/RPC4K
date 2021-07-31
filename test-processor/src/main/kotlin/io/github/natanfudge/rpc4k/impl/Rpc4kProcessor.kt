package io.github.natanfudge.rpc4k.impl

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.natanfudge.rpc4k.Api
import io.github.natanfudge.rpc4k.ProtocolDecoder
import io.github.natanfudge.rpc4k.RpcClient
import io.github.natanfudge.rpc4k.SerializationFormat
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.KSerializer
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


internal class Rpc4kProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> = invokeOnce(orElse = { emptyList() }) {
//        fib(42)
        val symbols = resolver.getSymbolsWithAnnotation(Api::class.qualifiedName!!).toList()
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
            addImport(Rpc4KGeneratedClientUtils::class,"send")
            addImport(Rpc4KGeneratedClientUtils::class,"sendFlow")

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

    private fun TypeSpec.Builder.addClientMethodImplementation(apiMethod: KSFunctionDeclaration) {
        val methodName = apiMethod.simpleName.asString()

        val returnType = apiMethod.returnType
        val returnTypeName = returnType!!.toTypeName()

//        val serializerType = returnTypeSerializer(returnType)

        addFunction(methodName) {
            addModifiers(KModifier.OVERRIDE)
            addClientMethodImplementationParameters(apiMethod)
            returns(returnTypeName)

            val parameterSerializers = apiMethod.parameters
                .map { "${it.name!!.asString()} to %FS".format.formatWith(it.type.toTypeName().serializerString()) }
                .join(",\n")

            //TODO: sendFlow when relevant
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
    // When using flows, we only serialize the elements individually
    private fun KSTypeReference.returnTypeSerializer(): FormattedString {
        val type = if (isFlow()) resolve().arguments[0].type else this
        return type!!.toTypeName().serializerString()
    }

    private fun KSTypeReference.isFlow() = resolve().declaration.qualifiedName?.asString() == Flow::class.qualifiedName

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
//            addImport(
//                "io.github.natanfudge.rpc4k.impl",
//                "Rpc4kGeneratedServerUtils.encodeResponse",
////                "Rpc4kGeneratedServerUtils"
//            )
            importClass(Rpc4kGeneratedServerUtils::class)
            importClass(KSerializer::class)
            importClass(Flow::class)

//            addImport()

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
            returns(Any::class)
            val routesFormatString = apiClass.getActualFunctions()
                .map { generateServerRouteDecoder(it) }.toList().join("\n")

            // p and r are shorthand methods
            val finalFormatString = """
        fun <T> p(serializer: KSerializer<T>, index: Int) =
            Rpc4kGeneratedServerUtils.decodeParameter(format, serializer, args[index])

        fun <T> r(serializer: KSerializer<T>, value: T) =
            Rpc4kGeneratedServerUtils.encodeResponse(format, serializer, value)

        fun <T> r(serializer: KSerializer<T>, value: Flow<T>) =
            Rpc4kGeneratedServerUtils.encodeFlowResponse(format, serializer, value)
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
        //TODO: encodeFlowResponse when relevant
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

    private fun generateServerParameterDecoder(index: Int, parameter: KSValueParameter): FormattedString {
        return """
            p(%FS, $index)
        """.trimIndent()
            .formatType(Rpc4kGeneratedServerUtils::class.asTypeName())
            .formatWith(parameter.type.toTypeName().serializerString())
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

    private fun ParameterizedTypeName.specialSerializer(name: String, typeArgumentAmount: Int): FormattedString {
        assert(typeArguments.size == typeArgumentAmount)

        return "$name(%FS)".formatWith(
            List(typeArgumentAmount) { typeArguments[it].serializerString() }.join(", ")
        )
    }

    private fun FileSpec.Builder.importBuiltinSerializers() {
        addImport(
            "kotlinx.serialization.builtins",
            "serializer",
            "nullable",
            "ListSerializer",
            "SetSerializer",
            "MapSerializer",
            "PairSerializer",
            "MapEntrySerializer",
            "TripleSerializer"
        )

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

