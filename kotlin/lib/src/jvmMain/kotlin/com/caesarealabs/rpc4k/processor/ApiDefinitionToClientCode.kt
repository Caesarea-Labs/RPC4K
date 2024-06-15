package com.caesarealabs.rpc4k.processor

import com.caesarealabs.rpc4k.processor.ApiDefinitionUtils.ignoreExperimentalWarnings
import com.caesarealabs.rpc4k.processor.utils.poet.*
import com.caesarealabs.rpc4k.runtime.api.GeneratedClientImplFactory
import com.caesarealabs.rpc4k.runtime.api.RpcClient
import com.caesarealabs.rpc4k.runtime.api.SerializationFormat
import com.caesarealabs.rpc4k.runtime.implementation.GeneratedCodeUtils
import com.caesarealabs.rpc4k.runtime.implementation.kotlinPoet
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import kotlinx.coroutines.flow.Flow

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
 * public class MyApiClientImpl(
 *     private val client: RpcClient,
 *     private val format: SerializationFormat,
 * ) {
 *     public suspend fun getDogs(num: Int, type: String): List<Dog> = request(
 *         client, format, "getDogs",
 *         listOf(num, type), listOf(Int.serializer(), String.serializer()),
 *         ListSerializer(Dog.serializer())
 *     )
 *
 *     public suspend fun putDog(dog: Dog): Unit = send(
 *         client, format, "putDog", listOf(dog),
 *         listOf(Dog.serializer())
 *     )
 * }
 * ```
 *
 * Which makes running client code much easier.
 */
internal object ApiDefinitionToClientCode {
    private const val ClientPropertyName = "client"
    private const val FormatPropertyName = "format"
    private val sendMethod = GeneratedCodeUtils::class.methodName("send")
    private val requestMethod = GeneratedCodeUtils::class.methodName("request")
    private val coldEventFlow = GeneratedCodeUtils::class.methodName("coldEventFlow")

    /**
     * @param userClassIsInterface When we are generating both a client and a server, it's useful to make the generated class
     * extend the user class. We need to know if the user class is an interface or not to properly extend/implement it.
     */
    fun convert(apiDefinition: RpcApi): FileSpec {
        val className = "${apiDefinition.name.simple}${GeneratedCodeUtils.ClientSuffix}"
        return fileSpec(GeneratedCodeUtils.Package, className) {
            // KotlinPoet doesn't handle extension methods well
            addImport("kotlinx.serialization.builtins", "serializer")
            addImport("kotlinx.serialization.builtins", "nullable")

            ignoreExperimentalWarnings()

//            addFunction(clientConstructorExtension(apiDefinition, className))

            addClass(className) {
                addType(factoryCompanionObject(className))

                addPrimaryConstructor {
                    addConstructorProperty(ClientPropertyName, type = RpcClient::class, KModifier.PRIVATE)
                    addConstructorProperty(FormatPropertyName, type = SerializationFormat::class, KModifier.PRIVATE)
                }
                for (method in apiDefinition.methods) addFunction(requestMethod(method))
                for (event in apiDefinition.events) addFunction(eventSubMethod(event))
            }
        }
    }


    /**
     * We generate a factory for the generated client implementation for it to be easy to just pass a [GeneratedClientImplFactory]
     * The generated code looks like this:
     * ```
     *     companion object Factory: GeneratedClientImplFactory<UserProtocol> {
     *         override fun build(client: RpcClient, format: SerializationFormat): UserProtocol {
     *             return UserProtocolClientImpl(client, format)
     *         }
     *     }
     * ```
     */
    private fun factoryCompanionObject(generatedClassName: String) = companionObject(GeneratedCodeUtils.FactoryName) {
        val generatedClientClass = ClassName(GeneratedCodeUtils.Package,generatedClassName)
        addSuperinterface(GeneratedClientImplFactory::class.asClassName().parameterizedBy(generatedClientClass))
        addFunction("build") {
            addModifiers(KModifier.OVERRIDE)
            addParameter(ClientPropertyName, RpcClient::class)
            addParameter(FormatPropertyName, SerializationFormat::class)
            returns(generatedClientClass)
            addStatement("return $generatedClassName($ClientPropertyName, $FormatPropertyName)")
        }
    }


//    /**
//     * Making the generated class available with an extension function makes it more resilient to name changes
//     *   since you will no longer need to directly reference the generated class.
//     *   Looks like:
//     *   ```
//     *   fun MyApi.Companion.client(client: RpcClient, format: SerializationFormat) = MyApiClientImpl(client,format)
//     *   ```
//     */
////    context(JvmContext)
//    private fun clientConstructorExtension(api: RpcApi, generatedClassName: String) =
//        extensionFunction(api.name.kotlinPoet.companion(), "client") {
//            addParameter(ClientPropertyName, RpcClient::class)
//            addParameter(FormatPropertyName, SerializationFormat::class)
//            returns(ClassName(GeneratedCodeUtils.Package, generatedClassName))
//            addStatement("return $generatedClassName($ClientPropertyName, $FormatPropertyName)")
//        }


    private fun requestMethod(rpcDefinition: RpcFunction): FunSpec = funSpec(rpcDefinition.name) {
        // We need to call network methods in this
        addModifiers(KModifier.SUSPEND/*, KModifier.OVERRIDE*/)

        for (argument in rpcDefinition.parameters) addParameter(requestParameter(argument))
        val returnType = rpcDefinition.returnType
        returns(returnType.typeName)

        val returnsValue = !returnType.isUnit
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


    private fun requestParameter(arg: RpcParameter): ParameterSpec {
        return ParameterSpec(arg.name, arg.type.typeName)
    }

    /**
     * Creates a method akin to
     * ```kotlin
     *         suspend fun eventTargetTest(normal: String, target: Int): Flow<String> {
     *         return createFlow(client, format, "eventTargetTest", listOf(normal), listOf(String.serializer()), String.serializer(), target)
     *     }
     * ```
     */
    private fun eventSubMethod(event: RpcEventEndpoint): FunSpec = funSpec(event.name) {
        // We need to call network methods in this (TODO: no we don't?)
//        addModifiers(KModifier.SUSPEND)

        for (argument in event.parameters) {
            if (!argument.isDispatch) addParameter(requestParameter(argument.value))
        }
        val returnType = Flow::class.asClassName().parameterizedBy(event.returnType.typeName)
        returns(returnType)

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

