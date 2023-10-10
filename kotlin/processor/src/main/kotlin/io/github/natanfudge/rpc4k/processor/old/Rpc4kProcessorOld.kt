package io.github.natanfudge.rpc4k.processor.old

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.*


@PublishedApi
internal const val GeneratedClientImplSuffix = "ClientImpl"

@PublishedApi
internal const val GeneratedServerImplSuffix = "Decoder"

internal class Rpc4kProcessorProviderOld : SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment
    ): SymbolProcessor = Rpc4kProcessorOld(environment)
}

internal class Rpc4kProcessorOld(private val env: SymbolProcessorEnvironment) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated>  {
        return listOf()
    }
//        invokeOnce(orElse = { emptyList() }) {
//            val symbols = resolver.getSymbolsWithAnnotation(Api::class.qualifiedName!!).toList()
//            println("Symbols: $symbols")
//            resolver.getSymbolsWithAnnotation(Api::class.qualifiedName!!)
//                .filter { it.validate() }
//                .filterIsInstance<KSClassDeclaration>()
//                .forEach { generateRpc(it) }
//
//            listOf()
//        }

//    private fun generateRpc(apiClass: KSClassDeclaration) {
//        val time = measureTimeMillis {
//            apiClass.getPublicApiFunctions().forEach { it.checkRequirements() }
//            ClientImplGenerator.generate(env, apiClass)
//            ServerDecoderGenerator.generate(env, apiClass)
//        }
//
//        env.logger.warn("Generated RPC classes for: ${apiClass.qualifiedName!!.asString()} in $time millis")
//        println("Generated RPC classes for: ${apiClass.qualifiedName!!.asString()} in $time millis")
//    }
//
//
//    private fun KSFunctionDeclaration.checkRequirements() {
//        checkRequirement(env, isOpen()) { "Api method must be open" }
//        checkRequirement(env, modifiers.contains(Modifier.SUSPEND)) { "Api method must be suspend" }
//        for (parameter in parameters) {
//            parameter.type.checkRequirements(returnType = false)
//        }
//        returnType?.checkRequirements(returnType = true)
//    }
//
//
//    private fun KSTypeReference.checkRequirements(returnType: Boolean) {
//        val type = resolve()
//
//        // In flows as a return type we serialize the type argument and not the flow itself
//        val serializedType = if (returnType && type.isFlow()) type.arguments[0].type!!.resolve() else type
//
//
//        checkRequirement(env, serializedType.isSerializable())
//        { "Type used in API method '${serializedType.declaration.qualifiedName!!.asString()}' must be Serializable" }
//    }
//
//    private var invoked = false
//
//    private fun <T> invokeOnce(orElse: () -> T, invocation: () -> T): T {
//        return if (!invoked) {
//            invoked = true
//            invocation()
//        } else orElse()
//    }
}

