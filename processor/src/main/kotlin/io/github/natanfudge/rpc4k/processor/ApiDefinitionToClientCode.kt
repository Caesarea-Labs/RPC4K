package io.github.natanfudge.rpc4k.processor

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import io.github.natanfudge.rpc4k.processor.old.addStatement
import io.github.natanfudge.rpc4k.processor.utils.classSpec
import io.github.natanfudge.rpc4k.processor.utils.fileSpec
import io.github.natanfudge.rpc4k.processor.utils.funSpec
import io.github.natanfudge.rpc4k.processor.utils.primaryConstructor
import io.github.natanfudge.rpc4k.runtime.api.RpcClient
import io.github.natanfudge.rpc4k.runtime.api.SerializationFormat

object ApiDefinitionToClientCode {
    private const val Package = "io.github.natanfudge.rpc4k.generated"
    private const val ClientPropertyName = "client"
    private const val FormatPropertyName = "format"
    fun convert(apiDefinition: ApiDefinition): FileSpec = fileSpec(Package, apiDefinition.name) {
        classSpec(apiDefinition.name) {
            primaryConstructor {
                constructorProperty(ClientPropertyName, type = RpcClient::class, KModifier.PRIVATE)
                constructorProperty(FormatPropertyName, type = SerializationFormat::class, KModifier.PRIVATE)
            }
        }
        for (method in apiDefinition.methods) addFunction(convertMethod(method))
    }

    private fun convertMethod(rpcDefinition: RpcDefinition): FunSpec = funSpec(rpcDefinition.name) {
        for (argument in rpcDefinition.args) addParameter(convertArgument(argument))
        returns(rpcDefinition.returnType)
        this.addStatement("""
            return %M($ClientPropertyName, $FormatPropertyName, %S, %M(${argumentValueList(rpcDefinition)}), %M()
            """.trimIndent())
    }

    private fun argumentValueList(rpc: RpcDefinition) = rpc.args.joinToString { it.name }
    private fun argumentSerializerList(rpc: RpcDefinition) = rpc.args.joinToString { it.name }

    private fun convertArgument(arg: RpcArgumentDefinition): ParameterSpec {
        return ParameterSpec(arg.name, arg.type)
    }
}

//class MyApiGeneratedClient(private val client: RpcClient, private val format: SerializationFormat) {
//    suspend fun getDogs(num: Int, type: String): List<Dog> {
//        return GeneratedCodeUtils.send(
//            client,
//            format,
//            "getDogs",
//            listOf(num, type),
//            listOf(Int.serializer(), String.serializer()),
//            ListSerializer(Dog.serializer())
//        )
//    }
//
//    suspend fun putDog(dog: Dog) {
//        GeneratedCodeUtils.send(client, format, "putDog", listOf(dog), listOf(Dog.serializer()))
//    }
//}