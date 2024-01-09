package com.caesarealabs.rpc4k.processor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.caesarealabs.rpc4k.processor.utils.poet.*
import com.caesarealabs.rpc4k.runtime.api.GeneratedClientImplFactory
import com.caesarealabs.rpc4k.runtime.api.RpcClient
import com.caesarealabs.rpc4k.runtime.api.SerializationFormat
import com.caesarealabs.rpc4k.runtime.implementation.GeneratedCodeUtils
import com.caesarealabs.rpc4k.runtime.implementation.kotlinPoet

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

    //TODO:


//    context(JvmContext)
    /**
     * @param userClassIsInterface When we are generating both a client and a server, it's useful to make the generated class
     * extend the user class. We need to know if the user class is an interface or not to properly extend/implement it.
     */
    fun convert(apiDefinition: RpcApi, userClassIsInterface: Boolean): FileSpec {
        val className = "${apiDefinition.name.simple}${GeneratedCodeUtils.ClientSuffix}"
        return fileSpec(GeneratedCodeUtils.Package, className) {
            // KotlinPoet doesn't handle extension methods well
            addImport("kotlinx.serialization.builtins", "serializer")
            addImport("kotlinx.serialization.builtins", "nullable")


            addFunction(clientConstructorExtension(apiDefinition, className))

            addClass(className) {
                addType(factoryCompanionObject(apiDefinition, className))

                addPrimaryConstructor {
                    addConstructorProperty(ClientPropertyName, type = RpcClient::class, KModifier.PRIVATE)
                    addConstructorProperty(FormatPropertyName, type = SerializationFormat::class, KModifier.PRIVATE)
                }
                val userClassName = apiDefinition.name.kotlinPoet
                if (userClassIsInterface) addSuperinterface(userClassName) else superclass(userClassName)
                for (method in apiDefinition.methods) addFunction(convertMethod(method))
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
//    context(JvmContext)
    private fun factoryCompanionObject(api: RpcApi, generatedClassName: String) = companionObject(GeneratedCodeUtils.FactoryName) {
        addSuperinterface(GeneratedClientImplFactory::class.asClassName().parameterizedBy(api.name.kotlinPoet))
        addFunction("build") {
            addModifiers(KModifier.OVERRIDE)
            addParameter(ClientPropertyName, RpcClient::class)
            addParameter(FormatPropertyName, SerializationFormat::class)
            returns(api.name.kotlinPoet)
            addStatement("return $generatedClassName($ClientPropertyName, $FormatPropertyName)")
        }
    }


    /**
     * Making the generated class available with an extension function makes it more resilient to name changes
     *   since you will no longer need to directly reference the generated class.
     *   Looks like:
     *   ```
     *   fun MyApi.Companion.client(client: RpcClient, format: SerializationFormat) = MyApiClientImpl(client,format)
     *   ```
     */
//    context(JvmContext)
    private fun clientConstructorExtension(api: RpcApi, generatedClassName: String) =
        extensionFunction(api.name.kotlinPoet.companion(), "client") {
            addParameter(ClientPropertyName, RpcClient::class)
            addParameter(FormatPropertyName, SerializationFormat::class)
            returns(ClassName(GeneratedCodeUtils.Package, generatedClassName))
            addStatement("return $generatedClassName($ClientPropertyName, $FormatPropertyName)")
        }

    private fun convertMethod(rpcDefinition: RpcFunction): FunSpec = funSpec(rpcDefinition.name) {
        // We need to call network methods in this
        addModifiers(KModifier.SUSPEND, KModifier.OVERRIDE)

        for (argument in rpcDefinition.parameters) addParameter(convertArgument(argument))
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

    private fun convertArgument(arg: RpcParameter): ParameterSpec {
        return ParameterSpec(arg.name, arg.type.typeName)
    }
}


