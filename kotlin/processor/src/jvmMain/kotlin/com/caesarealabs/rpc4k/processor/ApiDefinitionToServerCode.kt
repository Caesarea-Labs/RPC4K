package com.caesarealabs.rpc4k.processor

import com.caesarealabs.rpc4k.processor.ApiDefinitionUtils.ignoreExperimentalWarnings
import com.caesarealabs.rpc4k.processor.ApiDefinitionUtils.listOfEventSubSerializers
import com.caesarealabs.rpc4k.processor.utils.poet.*
import com.caesarealabs.rpc4k.runtime.api.HandlerConfig
import com.caesarealabs.rpc4k.runtime.api.RpcRouter
import com.caesarealabs.rpc4k.runtime.implementation.GeneratedCodeUtils
import com.caesarealabs.rpc4k.runtime.user.Rpc4kIndex
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

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
internal class ApiDefinitionToServerCode(private val api: RpcApi) {
    companion object {
        private const val Config: String = "config"
        private const val UserHandlerPropertyName = "handler"

        private const val RequestParamName = "request"
        private const val MethodParamName = "method"

        private val respondUtilsMethod = GeneratedCodeUtils::class.methodName("respond")
        private val invokeEventUtilsMethod = GeneratedCodeUtils::class.methodName("invokeEvent")
        private const val InvokerSuffix = "EventInvoker"
        private const val ParticipantsParamName = "participants"
        private val ParticipantsParamType = Set::class.parameterizedBy(String::class)
    }

    private val invokerName = "${api.name.simple}$InvokerSuffix"
    private val invokerClassName = ClassName(ApiDefinitionUtils.Package, invokerName)
    private val routerName = "${api.name.simple}${ApiDefinitionUtils.ServerSuffix}"
    private val routerClassName = ClassName(ApiDefinitionUtils.Package, routerName)
    private val clientClassName = ClassName(ApiDefinitionUtils.Package, api.name.simple + ApiDefinitionUtils.NetworkClientSuffix)
    private val serverClassName = api.name.kotlinPoet
    private val handlerConfig = HandlerConfig::class.asClassName().parameterizedBy(serverClassName)

    fun convert(): FileSpec {
        return fileSpec(ApiDefinitionUtils.Package, routerName) {
            // I know what I'm doing, Kotlin!
            addAnnotation(AnnotationSpec.builder(Suppress::class).addMember("%S", "UNCHECKED_CAST").build())
            ignoreExperimentalWarnings()

            // KotlinPoet doesn't handle extension methods well
            addImport("kotlinx.serialization.builtins", "serializer")
            addImport("kotlinx.serialization.builtins", "nullable")

            addProperty(rpc4kGeneratedSuiteExtension())

            addRouter()
            addInvokerClass()
        }
    }

    private fun FileSpec.Builder.addRouter() {
        addObject(routerName) {
            addSuperinterface(
                RpcRouter::class.asClassName().parameterizedBy(
                    serverClassName
                )
            )
            addFunction(handleRequestMethod())
        }
    }

    /**
     * val MyApi.Companion.server = object : GeneratedSuiteFactory<MyApi, AllEncompassingServiceClientImpl, AllEncompassingServiceEventInvoker> {
     *     override val createInvoker = ::AllEncompassingServiceEventInvoker
     *     override val createNetworkClient = ::AllEncompassingServiceClientImpl
     * }
     *
     */
    private fun rpc4kGeneratedSuiteExtension(): PropertySpec {
        val suiteType = Rpc4kIndex::class.asTypeName().parameterizedBy(serverClassName, clientClassName, invokerClassName)
        return extensionProperty(serverClassName.companion(), "rpc4k", suiteType) {
            //                            |   override val createMemoryClient get() = TODO()
            addCode(
                """
                            |return object: %T {
                            |   override val createInvoker = ::%T
                            |   override val createNetworkClient = ::%T
                            |   override val router = %T
                            |}
                            |""".trimMargin(),
                suiteType, invokerClassName, clientClassName, routerClassName
            )
        }
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
    private fun handleRequestMethod(): FunSpec = funSpec("routeRequest") {
        // This overrides GeneratedServerHandler
        addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)

        addParameter(RequestParamName, ByteArray::class)
        addParameter(MethodParamName, String::class)
        addParameter(Config, handlerConfig)

        returns(BYTE_ARRAY.copy(nullable = true))

        addControlFlow("return when($MethodParamName)") {
            for (method in api.methods) {
                addEndpointHandler(method)
            }
            addCode("else -> null\n")
        }
    }

    /**
     * Generates:
     * ```
     * respond(setup, request, listOf(Int.serializer(), String.serializer()), ListSerializer(Dog.serializer())) {
     *     setup.handler.getDogs(it[0] as Int, it[1] as String)
     * }
     * ```
     */
    private fun FunSpec.Builder.addEndpointHandler(rpc: RpcFunction) {
        addCode("%S -> ".formatWith(rpc.name))


        val arguments = listOf(
            Config,
            RequestParamName,
            ApiDefinitionUtils.listOfSerializers(rpc),
            rpc.returnType.toSerializerString()
        )

        addControlFlow(respondUtilsMethod.withArgumentList(arguments)) {
            functionHandleCall(rpc)
        }
    }

    private fun FunSpec.Builder.functionHandleCall(rpc: RpcFunction) {
        addStatement("$Config.$UserHandlerPropertyName.${rpc.name}".withMethodArguments(functionArguments(rpc)))
    }

    private fun functionArguments(rpc: RpcFunction) = rpc.parameters.mapIndexed { i, arg ->
        "it[$i] as %T".formatWith(arg.type.typeName)
    }


    /**
     * Generates something like this:
     * ```kotlin
     * class GeneratedEventInvokerExample: GeneratedEventInvoker<SmartEventResponder>() {
     *     suspend fun theoreticalGeneratedFunctionTest(title: String, row: AddedRow) {
     *         GeneratedCodeUtils.invokeEvent("test", listOf(row), title, setup)
     *     }
     *
     *     suspend fun theoreticalGeneratedFunctionTest2(row: TestEvent2Context) {
     *         GeneratedCodeUtils.invokeEvent("test2", listOf(row), null, setup)
     *     }
     * }
     * ```
     */
    private fun FileSpec.Builder.addInvokerClass() {
        addClass(invokerName) {

            addPrimaryConstructor {
                addConstructorProperty(Config, handlerConfig, KModifier.PRIVATE)
            }

            for (event in api.events) {
                addFunction(eventInvoker(event))
            }
        }
    }


    /**
     * ```kotlin
     *            GeneratedCodeUtils.invokeEvent(config, "eventTest",listOf(String.serializer()),String.serializer(),target.toString()) {
     *               config.handler.eventTest(dispatchParam, it[0] as String)
     *           }
     *     ```
     */
    private fun eventInvoker(event: RpcEventEndpoint) = funSpec("invoke${event.name.replaceFirstChar { it.uppercaseChar() }}") {
        val dispatchParameters = event.parameters.filter { it.isDispatch || it.isTarget }.map { it.value }
        addModifiers(KModifier.SUSPEND)
        for (parameter in dispatchParameters) {
            addParameter(parameter.name, parameter.type.typeName)
        }
        // Add support for not sending events to "participants"
        addParameter(ParameterSpec.builder(ParticipantsParamName, ParticipantsParamType).defaultValue("setOf()").build())
        addKdoc("@param $ParticipantsParamName Listeners that will not be invoked as they have caused the event.")
        //NiceToHave: watched object id

        val targetParameter = event.targetParameter?.name

        val paramSerializers = listOfEventSubSerializers(event)


        val target = if (targetParameter != null) "${targetParameter}.toString()" else ""
        val arguments = listOf(
            Config,
            "\"${event.name}\"",
            paramSerializers,
            event.returnType.toSerializerString(),
            ParticipantsParamName,
            target
        )
        addControlFlow(invokeEventUtilsMethod.withArgumentList(arguments)) {
            eventTransformCall(event)
        }
    }

    private fun FunSpec.Builder.eventTransformCall(rpc: RpcEventEndpoint) {
        addStatement("$Config.$UserHandlerPropertyName.${rpc.name}".withMethodArguments(eventArguments(rpc)))
    }

    private fun eventArguments(rpc: RpcEventEndpoint): List<FormattedString> {
        // We draw from both lists, according to which parameter is a dispatch param and which is an event param.
        var eventIndex = 0
        var dispatchIndex = 0
        val dispatchOrTargetParams = rpc.parameters.filter { it.isDispatch || it.isTarget }
        return rpc.parameters.map { parameter ->
            val dispatchValue = parameter.isDispatch || parameter.isTarget
            // We use the dispatch value for the @EventTarget value, which allows us to have the real value without serialization.
//            val targetList = if (dispatchValue) DispatcherDataParamName else "it"
            val index = if (dispatchValue) dispatchIndex++ else eventIndex++
            if (dispatchValue) dispatchOrTargetParams[index].value.name.formatString()
            else "it[$index] as %T".formatWith(parameter.value.type.typeName)
        }
    }
}


