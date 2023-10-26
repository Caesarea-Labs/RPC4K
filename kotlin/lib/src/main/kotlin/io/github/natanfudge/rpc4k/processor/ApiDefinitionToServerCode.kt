package io.github.natanfudge.rpc4k.processor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.natanfudge.rpc4k.processor.utils.poet.*
import io.github.natanfudge.rpc4k.runtime.api.GeneratedServerHelper
import io.github.natanfudge.rpc4k.runtime.api.RpcServerSetup
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
 * public fun BasicApi.Companion.server(): BasicApiServerImpl = BasicApiServerImpl()
 *
 * @Suppress("UNCHECKED_CAST")
 * public class BasicApiServerImpl: GeneratedServerHelper<BasicApi> {
 *     override suspend fun handle(request: ByteArray, method: String, setup: RpcSetup<BasicApi, *>): ByteArray? = when (method) {
 *         "getDogs" -> respond(setup, request, listOf(Int.serializer(), String.serializer()), ListSerializer(Dog.serializer())
 *         ) {
 *             setup.handler.getDogs(it[0] as Int, it[1] as String)
 *         }
 *
 *         "putDog" -> respond(setup, request, listOf(Dog.serializer()),
 *             VoidUnitSerializer()
 *         ) {
 *             setup.handler.putDog(it[0] as Dog)
 *         }
 *
 *         else -> null
 *     }
 * }
 * ```
 *
 * Which makes running client code much easier.
 */
object ApiDefinitionToServerCode {
    private const val SetupParamName: String = "setup"
    private const val UserHandlerPropertyName = "handler"
    private val wildcardType = WildcardTypeName.producerOf(ANY.copy(nullable = true))

    //    private const val ApiPropertyName = "api"
//    private const val FormatPropertyName = "format"
//    private const val ServerPropertyName = "server"
    private const val RequestParamName = "request"
    private const val MethodParamName = "method"
//    private val handleUtilsMethod = GeneratedCodeUtils::class.methodName("withCatching")
    private val respondUtilsMethod = GeneratedCodeUtils::class.methodName("respond")

    fun convert(apiDefinition: ApiDefinition): FileSpec {
        val className = "${apiDefinition.name.simpleName}${GeneratedCodeUtils.ServerSuffix}"
        return fileSpec(GeneratedCodeUtils.Package, className) {
            // KotlinPoet doesn't handle extension methods well
            addImport("kotlinx.serialization.builtins", "serializer")
            addImport("kotlinx.serialization.builtins", "nullable")

            addFunction(serverConstructorExtension(apiDefinition, generatedClassName = className))

            addClass(className) {
                // I know what I'm doing, Kotlin!
                addAnnotation(AnnotationSpec.builder(Suppress::class).addMember("%S", "UNCHECKED_CAST").build())

//                addType(factoryCompanionObject(apiDefinition, generatedClassName = className))

                addSuperinterface(GeneratedServerHelper::class.asClassName().parameterizedBy(apiDefinition.name.className))
//                addPrimaryConstructor {
//                    addConstructorProperty(ApiPropertyName, type = apiDefinition.name.className, KModifier.PRIVATE)
//                    addConstructorProperty(FormatPropertyName, type = SerializationFormat::class, KModifier.PRIVATE)
//                    addConstructorProperty(ServerPropertyName, type = RpcServer::class, KModifier.PRIVATE)
//                }
                addFunction(handleMethod(apiDefinition))
            }
        }
    }

    /**
     * We generate a factory for the generated server implementation for it to be easy to just pass a [GeneratedServerHandlerFactory]
     * The generated code looks like this:
     * ```
     *     companion object Factory : GeneratedServerHandlerFactory<UserProtocol> {
     *         override fun build(api: UserProtocol, format: SerializationFormat, server: RpcServer): GeneratedServerHandler {
     *             return UserProtocolServerImpl(api, format, server)
     *         }
     *     }
     * ```
     */
//    private fun factoryCompanionObject(api: ApiDefinition, generatedClassName: String) = companionObject(GeneratedCodeUtils.FactoryName) {
//        val userClassName = api.name.className
//        addSuperinterface(GeneratedServerHandlerFactory::class.asClassName().parameterizedBy(userClassName))
//        addFunction("build") {
//            addModifiers(KModifier.OVERRIDE)
//            addParameter(ApiPropertyName, userClassName)
//            addParameter(FormatPropertyName, SerializationFormat::class)
//            addParameter(ServerPropertyName, RpcServer::class)
//            returns(GeneratedServerHelper::class)
//            addStatement("return $generatedClassName($ApiPropertyName, $FormatPropertyName, $ServerPropertyName)")
//        }
//    }

    /**
     * Making the generated class available with an extension function makes it more resilient to name changes
     *   since you will no longer need to directly reference the generated class.
     *   Looks like:
     *   ```
     *   fun MyApi.Companion.server(api: MyApi, format: SerializationFormat, server: RpcServer) = MyApiServerImpl(api, format, server)
     *   ```
     */
    private fun serverConstructorExtension(api: ApiDefinition, generatedClassName: String) =
        extensionFunction(api.name.className.companion(), "server") {
//            addParameter(ApiPropertyName, api.name.className)
//            addParameter(FormatPropertyName, SerializationFormat::class)
//            addParameter(ServerPropertyName, RpcServer::class)
            returns(ClassName(GeneratedCodeUtils.Package, generatedClassName))
            addStatement("return $generatedClassName()")
        }

    /**
     * Generates:
     * ```
     *     override suspend fun handle(request: ByteArray, method: String, setup: RpcSetup<BasicApi, *>): ByteArray? = when (method) {
     *         "getDogs" -> respond(setup, request, listOf(Int.serializer(), String.serializer()), ListSerializer(Dog.serializer())
     *         ) {
     *             setup.handler.getDogs(it[0] as Int, it[1] as String)
     *         }
     *
     *         "putDog" -> respond(setup, request, listOf(Dog.serializer()),
     *             VoidUnitSerializer()
     *         ) {
     *             setup.handler.putDog(it[0] as Dog)
     *         }
     *
     *         else -> null
     *     }
     * ```
     */
    private fun handleMethod(api: ApiDefinition): FunSpec = funSpec("handle") {
        // This overrides GeneratedServerHandler
        addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)

        addParameter(RequestParamName, ByteArray::class)
        addParameter(MethodParamName, String::class)
        addParameter(SetupParamName,
            RpcServerSetup::class.asClassName().parameterizedBy(api.name.className, wildcardType)
        )

        returns(BYTE_ARRAY.copy(nullable = true))

//        addControlFlow("%M($ServerPropertyName)", handleUtilsMethod) {
            addControlFlow("return when($MethodParamName)") {
                for (method in api.methods) {
                    addEndpointHandler(method)
                }
                addCode("else -> null\n")
            }
//        }
    }

    /**
     * Generates:
     * ```
     * respond(setup, request, listOf(Int.serializer(), String.serializer()), ListSerializer(Dog.serializer())) {
     *     setup.handler.getDogs(it[0] as Int, it[1] as String)
     * }
     * ```
     */
    private fun FunSpec.Builder.addEndpointHandler(rpc: RpcDefinition) {
        addCode("%S -> ".formatWith(rpc.name))


        val arguments = listOf(
            SetupParamName,
            RequestParamName,
            ApiDefinitionConverters.listOfSerializers(rpc),
            rpc.returnType.toSerializerString()
        )

        addControlFlow(respondUtilsMethod.withArgumentList(arguments)) {
            addStatement("$SetupParamName.$UserHandlerPropertyName.${rpc.name}".withMethodArguments(endpointArguments(rpc)))
        }
    }

    private fun endpointArguments(rpc: RpcDefinition) = rpc.parameters.mapIndexed { i, arg ->
        "it[$i] as %T".formatWith(arg.type.typeName)
    }
}


