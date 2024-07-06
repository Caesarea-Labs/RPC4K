//LOWPRIO: Improve server testing with "in-memory-server" client generation
// package com.caesarealabs.rpc4k.processor
// import com.caesarealabs.rpc4k.processor.ApiDefinitionUtils.experimentalUnsignedTypes
// import com.caesarealabs.rpc4k.processor.ApiDefinitionUtils.optIn
// import com.caesarealabs.rpc4k.processor.utils.poet.addInterface
// import com.caesarealabs.rpc4k.processor.utils.poet.fileSpec
// import com.squareup.kotlinpoet.AnnotationSpec
// import com.squareup.kotlinpoet.FileSpec
// import com.squareup.kotlinpoet.FunSpec
// import com.squareup.kotlinpoet.KModifier
//
/// **
// * Converts
// * ```
// * @Api
// * class MyApi {
// *     private val dogs = mutableListOf<Dog>()
// *     fun getDogs(num: Int, type: String): List<Dog> {
// *         return dogs.filter { it.type == type }.take(num)
// *     }
// *
// *     fun putDog(dog: Dog) {
// *         dogs.add(dog)
// *     }
// *
// *     @RpcEvent fun dogEvent(@Dispatch dispatchParam: Int, @EventTarget target: String, clientParam: Boolean): Int {
// *          return if (clientParam) dispatchParam else dispatchParam + 2
// *     }
// * }
// * ```
// * into
// * ```
// * interface MyApiClient {
// *     suspend fun getDogs(num: Int, type: String): List<Dog>
// *
// *     suspend fun putDog(dog: Dog): Unit
// *
// *     suspend fun dogEvent(clientParam: Boolean): EventSubscription<Int>
// * }
// * ```
// *
// * Which makes running client code much easier.
// */
//internal object ApiDefinitionToClientInterface {
//    fun interfaceName(apiDefinition: RpcApi) = "${apiDefinition.name.simple}${ApiDefinitionUtils.ClientInterfaceSuffix}"
//    fun convert(apiDefinition: RpcApi): FileSpec {
//        val generatedName = interfaceName(apiDefinition)
//        return fileSpec(ApiDefinitionUtils.Package, generatedName) {
//            addAnnotation(AnnotationSpec.builder(optIn).addMember("%T::class", experimentalUnsignedTypes).build())
//
//            addInterface(generatedName) {
//                for (method in apiDefinition.methods) addFunction(requestMethod(method))
//                for (event in apiDefinition.events) addFunction(eventSubMethod(event))
//            }
//        }
//    }
//
//
//    /**
//     * Creates a method like
//     * ```
//     *       suspend fun getDogs(num: Int, type: String): List<Dog>
//     *  ```
//     */
//    private fun requestMethod(rpcDefinition: RpcFunction): FunSpec = ApiDefinitionToClient.createRequest(rpcDefinition) {
//        addModifiers(KModifier.ABSTRACT)
//    }
//
//
//    /**
//     * Creates a method akin to
//     * ```kotlin
//     *   suspend fun eventTargetTest(normal: String, target: Int): EventSubscription<String>
//     * ```
//     */
//    private fun eventSubMethod(event: RpcEventEndpoint): FunSpec = ApiDefinitionToClient.createEventSubscription(event) {
//        addModifiers(KModifier.ABSTRACT)
//    }
//
//}
//
