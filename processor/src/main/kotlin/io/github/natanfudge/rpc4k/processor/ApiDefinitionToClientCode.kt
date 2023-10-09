package io.github.natanfudge.rpc4k.processor

import com.squareup.kotlinpoet.*
import io.github.natanfudge.rpc4k.processor.utils.poet.*
import io.github.natanfudge.rpc4k.processor.utils.toSerializerString
import io.github.natanfudge.rpc4k.runtime.api.RpcClient
import io.github.natanfudge.rpc4k.runtime.api.SerializationFormat
import io.github.natanfudge.rpc4k.runtime.implementation.GeneratedCodeUtils

////TODO:
//// Add .client(a,b) and .server(a,b,c) extension methods to reference the generated classes (this makes it more resilient to name changes)
//// Add .ClientFactory and .ServerFactory objects to reference the constructors of the generated classes (this makes it easier to have generic stuff)
//// Make Client generated class extend annotated class
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
object ApiDefinitionToClientCode {
    private const val ClientPropertyName = "client"
    private const val FormatPropertyName = "format"
    private val sendMethod = GeneratedCodeUtils::class.methodName("send")
    private val requestMethod = GeneratedCodeUtils::class.methodName("request")

    fun convert(apiDefinition: ApiDefinition): FileSpec {
        val className = "${apiDefinition.name}${GeneratedCodeUtils.ClientSuffix}"
        return fileSpec(GeneratedCodeUtils.Package, className) {
            // KotlinPoet doesn't handle extension methods well
            addImport("kotlinx.serialization.builtins", "serializer")


            addFunction(clientConstructorExtension(apiDefinition, className))

            addClass(className) {
                addPrimaryConstructor {
                    addConstructorProperty(ClientPropertyName, type = RpcClient::class, KModifier.PRIVATE)
                    addConstructorProperty(FormatPropertyName, type = SerializationFormat::class, KModifier.PRIVATE)
                }
                val superClassName = apiDefinition.toClassName()
                if (apiDefinition.isInterface) addSuperinterface(superClassName) else superclass(superClassName)
                for (method in apiDefinition.methods) addFunction(convertMethod(method))
            }
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
    private fun clientConstructorExtension(api: ApiDefinition, generatedClassName: String) = extensionFunction(api.toCompanionClassName(), "client") {
        addParameter(ClientPropertyName, RpcClient::class)
        addParameter(FormatPropertyName, SerializationFormat::class)
        returns(ClassName(GeneratedCodeUtils.Package, generatedClassName))
        addStatement("return $generatedClassName($ClientPropertyName, $FormatPropertyName)")
    }
    //TODO: write codegen for client factory

    private fun convertMethod(rpcDefinition: RpcDefinition): FunSpec = funSpec(rpcDefinition.name) {
        // We need to call network methods in this
        addModifiers(KModifier.SUSPEND, KModifier.OVERRIDE)

        for (argument in rpcDefinition.args) addParameter(convertArgument(argument))
        val returnType = rpcDefinition.returnType.asTypeName()
        returns(returnType)

        val returnsValue = returnType != UNIT
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
            ApiDefinitionConverters.listOfFunction.withArgumentList(rpcDefinition.args.map { it.name }),
            ApiDefinitionConverters.listOfSerializers(rpcDefinition),
        )

        if (returnsValue) arguments.add(rpcDefinition.returnType.toSerializerString())

        this.addStatement("return ".plusFormat(method.withArgumentList(arguments)))
    }

    private fun convertArgument(arg: RpcArgumentDefinition): ParameterSpec {
        return ParameterSpec(arg.name, arg.type.asTypeName())
    }
}


