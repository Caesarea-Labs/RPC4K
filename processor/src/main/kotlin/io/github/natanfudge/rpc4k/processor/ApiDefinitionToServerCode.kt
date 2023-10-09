package io.github.natanfudge.rpc4k.processor

import com.squareup.kotlinpoet.*
import io.github.natanfudge.rpc4k.processor.utils.poet.*
import io.github.natanfudge.rpc4k.processor.utils.toSerializerString
import io.github.natanfudge.rpc4k.runtime.api.RpcServer
import io.github.natanfudge.rpc4k.runtime.api.SerializationFormat
import io.github.natanfudge.rpc4k.runtime.implementation.GeneratedCodeUtils
import io.github.natanfudge.rpc4k.runtime.implementation.GeneratedServerHandler

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
    private const val apiPropertyName = "api"
    private const val FormatPropertyName = "format"
    private const val ServerPropertyName = "server"
    private const val RequestParamName = "request"
    private const val MethodParamName = "method"
    private val handleUtilsMethod = GeneratedCodeUtils::class.methodName("withCatching")
    private val respondUtilsMethod = GeneratedCodeUtils::class.methodName("respond")

    fun convert(apiDefinition: ApiDefinition): FileSpec {
        val className = "${apiDefinition.name}ServerImpl"
        return fileSpec(ApiDefinitionConverters.Package, className) {
            // KotlinPoet doesn't handle extension methods well
            addImport("kotlinx.serialization.builtins", "serializer")
            addClass(className) {
                // I know what I'm doing, Kotlin!
                addAnnotation(AnnotationSpec.builder(Suppress::class).addMember("%S", "UNCHECKED_CAST").build())

                addSuperinterface(GeneratedServerHandler::class)
                addPrimaryConstructor {
                    addConstructorProperty(apiPropertyName, type = ClassName(apiDefinition.implementationPackageName, apiDefinition.name), KModifier.PRIVATE)
                    addConstructorProperty(FormatPropertyName, type = SerializationFormat::class, KModifier.PRIVATE)
                    addConstructorProperty(ServerPropertyName, type = RpcServer::class, KModifier.PRIVATE)
                }
                addFunction(handleMethod(apiDefinition))
            }
        }
    }

    private fun handleMethod(api: ApiDefinition): FunSpec = funSpec("handle") {
        // This overrides GeneratedServerHandler
        addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)

        addParameter(RequestParamName, ByteArray::class)
        addParameter(MethodParamName, String::class)

        addControlFlow("%M($ServerPropertyName)", handleUtilsMethod) {
            addControlFlow("when($MethodParamName)") {
                for(method in api.methods) {
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
            addStatement("$apiPropertyName.${rpc.name}".formatString().withMethodArguments(endpointArguments(rpc)))
        }
    }

    private fun endpointArguments(rpc: RpcDefinition) = rpc.args.mapIndexed { i, arg ->
        "it[$i] as %T".formatWith(arg.type.asTypeName())
    }
}


