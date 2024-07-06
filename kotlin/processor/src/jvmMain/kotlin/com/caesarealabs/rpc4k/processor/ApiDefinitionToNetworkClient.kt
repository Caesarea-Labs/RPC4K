package com.caesarealabs.rpc4k.processor

import com.caesarealabs.rpc4k.processor.ApiDefinitionUtils.ignoreExperimentalWarnings
import com.caesarealabs.rpc4k.processor.utils.poet.*
import com.caesarealabs.rpc4k.runtime.api.GeneratedClientImplFactory
import com.caesarealabs.rpc4k.runtime.api.RpcClient
import com.caesarealabs.rpc4k.runtime.api.SerializationFormat
import com.caesarealabs.rpc4k.runtime.implementation.GeneratedCodeUtils
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
 *
 *     @RpcEvent fun dogEvent(@Dispatch dispatchParam: Int, @EventTarget target: String, clientParam: Boolean): Int {
 *          return if (clientParam) dispatchParam else dispatchParam + 2
 *     }
 * }
 * ```
 * into
 * ```
 * class MyApiNetworkClient(
 *     private val client: RpcClient,
 *     private val format: SerializationFormat,
 * ) {
 *     suspend fun getDogs(num: Int, type: String): List<Dog> = request(
 *         client, format, "getDogs",
 *         listOf(num, type), listOf(Int.serializer(), String.serializer()),
 *         ListSerializer(Dog.serializer())
 *     )
 *
 *     suspend fun putDog(dog: Dog): Unit = send(
 *         client, format, "putDog", listOf(dog),
 *         listOf(Dog.serializer())
 *     )
 *
 *      override fun dogEvent(target: String, clientParam: Boolean): EventSubscription<Int> =
 *       coldEventFlow(client, format, "dogEvent", listOf(clientParam), listOf(Boolean.serializer()),
 *       Int.serializer(), target)
 * }
 * ```
 *
 * Which makes running client code much easier.
 */
internal object ApiDefinitionToNetworkClient {
    private const val ClientPropertyName = "client"
    private const val FormatPropertyName = "format"
    private val sendMethod = GeneratedCodeUtils::class.methodName("send")
    private val requestMethod = GeneratedCodeUtils::class.methodName("request")
    private val coldEventFlow = GeneratedCodeUtils::class.methodName("coldEventFlow")

    fun convert(apiDefinition: RpcApi): FileSpec {
        val className = "${apiDefinition.name.simple}${ApiDefinitionUtils.NetworkClientSuffix}"
        return fileSpec(ApiDefinitionUtils.Package, className) {
            // KotlinPoet doesn't handle extension methods well
            addImport("kotlinx.serialization.builtins", "serializer")
            addImport("kotlinx.serialization.builtins", "nullable")

            ignoreExperimentalWarnings()

            addClass(className) {
                //LOWPRIO: Improve server testing with "in-memory-server" client generation
                // addSuperinterface(ClassName(ApiDefinitionUtils.Package, ApiDefinitionToClientInterface.interfaceName(apiDefinition)))
                // addType(factoryCompanionObject(className))

                addPrimaryConstructor {
                    addConstructorProperty(ClientPropertyName, type = RpcClient::class, KModifier.PRIVATE)
                    addConstructorProperty(FormatPropertyName, type = SerializationFormat::class, KModifier.PRIVATE)
                }
                for (method in apiDefinition.methods) addFunction(requestMethod(method))
                for (event in apiDefinition.events) addFunction(eventSubMethod(event))
            }
        }
    }


//    /**
//     * We generate a factory for the generated client implementation for it to be easy to just pass a [GeneratedClientImplFactory]
//     * The generated code looks like this:
//     * ```
//     *     companion object Factory: GeneratedClientImplFactory<UserProtocol> {
//     *         override fun build(client: RpcClient, format: SerializationFormat): UserProtocol {
//     *             return UserProtocolClientImpl(client, format)
//     *         }
//     *     }
//     * ```
//     */
//    private fun factoryCompanionObject(generatedClassName: String) = companionObject(ApiDefinitionUtils.FactoryName) {
//        val generatedClientClass = ClassName(ApiDefinitionUtils.Package, generatedClassName)
//        addSuperinterface(GeneratedClientImplFactory::class.asClassName().parameterizedBy(generatedClientClass))
//        addFunction("build") {
//            addModifiers(KModifier.OVERRIDE)
//            addParameter(ClientPropertyName, RpcClient::class)
//            addParameter(FormatPropertyName, SerializationFormat::class)
//            returns(generatedClientClass)
//            addStatement("return $generatedClassName($ClientPropertyName, $FormatPropertyName)")
//        }
//    }


    /**
     * Creates a method like
     * ```
     *       suspend fun getDogs(num: Int, type: String): List<Dog> = request(
     *           client, format, "getDogs",
     *           listOf(num, type), listOf(Int.serializer(), String.serializer()),
     *           ListSerializer(Dog.serializer())
     *       )
     *
     *  ```
     */
    private fun requestMethod(rpcDefinition: RpcFunction): FunSpec = ApiDefinitionToClient.createRequest(rpcDefinition) {
        addModifiers(KModifier.OVERRIDE)
        val returnsValue = !rpcDefinition.returnType.isUnit
        // We use a simpler method where no return type is required
        val method = if (returnsValue) requestMethod else sendMethod

        // Example:
        //        return GeneratedCodeUtils.request(
        //            client,
        //            format,
        //            "getDogs",
        //            listOf(num, type),
        //            listOf(Int.serializer(), String.serializer()),
        //            ListSerializer(Dog.serializer())
        //        )
        val arguments = mutableListOf(
            ClientPropertyName,
            FormatPropertyName,
            "%S".formatWith(rpcDefinition.name),
            ApiDefinitionUtils.listOfFunction.withArgumentList(rpcDefinition.parameters.map { it.name }),
            ApiDefinitionUtils.listOfSerializers(rpcDefinition),
        )

        if (returnsValue) arguments.add(rpcDefinition.returnType.toSerializerString())

        this.addStatement("return ".plusFormat(method.withArgumentList(arguments)))
    }


    /**
     * Creates a method akin to
     * ```kotlin
     *         suspend fun eventTargetTest(normal: String, target: Int): EventSubscription<String> {
     *         return createFlow(client, format, "eventTargetTest", listOf(normal), listOf(String.serializer()), String.serializer(), target)
     *     }
     * ```
     */
    private fun eventSubMethod(event: RpcEventEndpoint): FunSpec = ApiDefinitionToClient.createEventSubscription(event) {
        addModifiers(KModifier.OVERRIDE)
        val normalArgs = event.parameters.filter { !it.isDispatch && !it.isTarget }.map { it.value.name }


        val arguments = mutableListOf(
            ClientPropertyName,
            FormatPropertyName,
            "%S".formatWith(event.name),
//            We only need the dispatch parameters
            if (normalArgs.isEmpty()) "listOf<Nothing>()" else ApiDefinitionUtils.listOfFunction.withArgumentList(normalArgs),
            ApiDefinitionUtils.listOfEventSubSerializers(event),
            event.returnType.toSerializerString()
        )

        val targetParameter = event.parameters.find { it.isTarget }

        // Pass the target if its relevant
        if (targetParameter != null) arguments.add(targetParameter.value.name)

        this.addStatement("return ".plusFormat(coldEventFlow.withArgumentList(arguments)))
    }

}

