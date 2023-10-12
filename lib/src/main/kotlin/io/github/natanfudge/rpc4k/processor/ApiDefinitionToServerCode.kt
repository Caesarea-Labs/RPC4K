package io.github.natanfudge.rpc4k.processor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.natanfudge.rpc4k.processor.utils.poet.*
import io.github.natanfudge.rpc4k.runtime.api.GeneratedServerImpl
import io.github.natanfudge.rpc4k.runtime.api.GeneratedServerImplFactory
import io.github.natanfudge.rpc4k.runtime.api.RpcServer
import io.github.natanfudge.rpc4k.runtime.api.SerializationFormat
import io.github.natanfudge.rpc4k.runtime.implementation.GeneratedCodeUtils

/**
 * Converts
 * ```
 * @Api
 * class MyApi {
 *     private val dogs = mutableListOf<Dog>()
 *     fun getDogs(num: Int, type: String): List<Dog> {
 *         return dogs.filter { it.type == type }.take(num)
 *     }
 *
 *     fun putDog(dog: Dog) {
 *         dogs.add(dog)
 *     }
 * }
 * ```
 * into
 * ```
 * class MyApiServerImpl(private val api: MyApi, private val format: SerializationFormat, private val server: RpcServer): GeneratedServerHandler {
 *     override suspend fun handle(request: ByteArray, method: String) {
 *         GeneratedCodeUtils.handle(server) {
 *             when (method) {
 *                 "getDogs" -> GeneratedCodeUtils.respond(
 *                     format,
 *                     server,
 *                     request,
 *                     listOf(Int.serializer(), String.serializer()),
 *                     ListSerializer(Dog.serializer())
 *                 ) {
 *                     api.getDogs(it[0] as Int, it[1] as String)
 *                 }
 *
 *                 "putDog" -> GeneratedCodeUtils.respond(format, server, request, listOf(Dog.serializer()), Unit.serializer()) {
 *                     api.putDog(it[0] as Dog)
 *                 }
 *             }
 *         }
 *     }
 * }
 *
 * ```
 *
 * Which makes running client code much easier.
 */
object ApiDefinitionToServerCode {
    private const val ApiPropertyName = "api"
    private const val FormatPropertyName = "format"
    private const val ServerPropertyName = "server"
    private const val RequestParamName = "request"
    private const val MethodParamName = "method"
    private val handleUtilsMethod = GeneratedCodeUtils::class.methodName("withCatching")
    private val respondUtilsMethod = GeneratedCodeUtils::class.methodName("respond")

    context(JvmContext)
    fun convert(apiDefinition: ApiDefinition): FileSpec {
        val className = "${apiDefinition.name}${GeneratedCodeUtils.ServerSuffix}"
        return fileSpec(GeneratedCodeUtils.Package, className) {
            // KotlinPoet doesn't handle extension methods well
            addImport("kotlinx.serialization.builtins", "serializer")
            addImport("kotlinx.serialization.builtins", "nullable")

            addFunction(serverConstructorExtension(generatedClassName = className))

            addClass(className) {
                // I know what I'm doing, Kotlin!
                addAnnotation(AnnotationSpec.builder(Suppress::class).addMember("%S", "UNCHECKED_CAST").build())

                addType(factoryCompanionObject(generatedClassName = className))

                addSuperinterface(GeneratedServerImpl::class)
                addPrimaryConstructor {
                    addConstructorProperty(ApiPropertyName, type = userClassName, KModifier.PRIVATE)
                    addConstructorProperty(FormatPropertyName, type = SerializationFormat::class, KModifier.PRIVATE)
                    addConstructorProperty(ServerPropertyName, type = RpcServer::class, KModifier.PRIVATE)
                }
                addFunction(handleMethod(apiDefinition))
            }
        }
    }

    /**
     * We generate a factory for the generated server implementation for it to be easy to just pass a [GeneratedServerImplFactory]
     * The generated code looks like this:
     * ```
     *     companion object Factory : GeneratedServerHandlerFactory<UserProtocol> {
     *         override fun build(api: UserProtocol, format: SerializationFormat, server: RpcServer): GeneratedServerHandler {
     *             return UserProtocolServerImpl(api, format, server)
     *         }
     *     }
     * ```
     */
    context(JvmContext)
    private fun factoryCompanionObject(generatedClassName: String) =
        companionObject(GeneratedCodeUtils.FactoryName) {
            addSuperinterface(GeneratedServerImplFactory::class.asClassName().parameterizedBy(userClassName))
            addFunction("build") {
                addModifiers(KModifier.OVERRIDE)
                addParameter(ApiPropertyName, userClassName)
                addParameter(FormatPropertyName, SerializationFormat::class)
                addParameter(ServerPropertyName, RpcServer::class)
                returns(GeneratedServerImpl::class)
                addStatement("return $generatedClassName($ApiPropertyName, $FormatPropertyName, $ServerPropertyName)")
            }
        }

    /**
     * Making the generated class available with an extension function makes it more resilient to name changes
     *   since you will no longer need to directly reference the generated class.
     *   Looks like:
     *   ```
     *   fun MyApi.Companion.server(api: MyApi, format: SerializationFormat, server: RpcServer) = MyApiServerImpl(api, format, server)
     *   ```
     */
    context(JvmContext)
    private fun serverConstructorExtension(generatedClassName: String) =
        extensionFunction(userCompanionClassName, "server") {
            addParameter(ApiPropertyName, userClassName)
            addParameter(FormatPropertyName, SerializationFormat::class)
            addParameter(ServerPropertyName, RpcServer::class)
            returns(ClassName(GeneratedCodeUtils.Package, generatedClassName))
            addStatement("return $generatedClassName($ApiPropertyName, $FormatPropertyName, $ServerPropertyName)")
        }

    private fun handleMethod(api: ApiDefinition): FunSpec = funSpec("handle") {
        // This overrides GeneratedServerHandler
        addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)

        addParameter(RequestParamName, ByteArray::class)
        addParameter(MethodParamName, String::class)

        addControlFlow("%M($ServerPropertyName)", handleUtilsMethod) {
            addControlFlow("when($MethodParamName)") {
                for (method in api.methods) {
                    addEndpointHandler(method)
                }
            }
        }
    }

    private fun FunSpec.Builder.addEndpointHandler(rpc: RpcDefinition) {
        addCode("%S -> ".formatWith(rpc.name))


        val arguments = listOf(
            FormatPropertyName,
            ServerPropertyName,
            RequestParamName,
            ApiDefinitionConverters.listOfSerializers(rpc),
            rpc.returnType.toSerializerString()
        )

        addControlFlow(respondUtilsMethod.withArgumentList(arguments)) {
            addStatement("$ApiPropertyName.${rpc.name}".formatString().withMethodArguments(endpointArguments(rpc)))
        }
    }

    private fun endpointArguments(rpc: RpcDefinition) = rpc.args.mapIndexed { i, arg ->
        "it[$i] as %T".formatWith(arg.type.typeName)
    }
}


