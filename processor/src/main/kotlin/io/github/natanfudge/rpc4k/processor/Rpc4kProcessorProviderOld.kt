package io.github.natanfudge.rpc4k.processor

import com.google.devtools.ksp.isOpen
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import io.github.natanfudge.rpc4k.processor.old.*
import io.github.natanfudge.rpc4k.runtime.api.Api
import kotlin.system.measureTimeMillis

internal class Rpc4kProcessorProvider : SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment
    ): SymbolProcessor = Rpc4kProcessor(environment)
}

internal class Rpc4kProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {
    //TODO: make sure to use the kotlinpoet ksp integration and see stuff is happening incrementally

    override fun process(resolver: Resolver): List<KSAnnotated> {
        env.logger.info("Processing @Api")
        val time = measureTimeMillis {
            val symbols = resolver.getSymbolsWithAnnotation(Api::class.qualifiedName!!).toList()
            println("Symbols: $symbols")
            for (symbol in resolver.getSymbolsWithAnnotation(Api::class.qualifiedName!!)) {
                if (symbol.validate()) {
                    // Api is only applicable to classes
                    generateRpc(symbol as KSClassDeclaration)
                }
            }
        }
        println("Gemerated RPC classes in ${time}ms")
        //TODO: make sure process() only gets called once

        // Nothing needs to be deferred
        return listOf()
    }

    private fun generateRpc(apiClass: KSClassDeclaration) {
        val time = measureTimeMillis {
            apiClass.getPublicApiFunctions().forEach { it.checkRequirements() }
            ClientImplGenerator.generate(env, apiClass)
            ServerDecoderGenerator.generate(env, apiClass)
        }

        env.logger.warn("Generated RPC classes for: ${apiClass.qualifiedName!!.asString()} in $time millis")
        println("Generated RPC classes for: ${apiClass.qualifiedName!!.asString()} in $time millis")
    }


    private fun KSFunctionDeclaration.checkRequirements() {
        checkRequirement(env, modifiers.contains(Modifier.SUSPEND)) { "Api method must be suspend" }
        for (parameter in parameters) {
            parameter.type.checkRequirements(returnType = false)
        }
        returnType?.checkRequirements(returnType = true)
    }


    private fun KSTypeReference.checkRequirements(returnType: Boolean) {
        val type = resolve()

        // In flows as a return type we serialize the type argument and not the flow itself
        val serializedType = if (returnType && type.isFlow()) type.arguments[0].type!!.resolve() else type


        checkRequirement(env, serializedType.isSerializable())
        { "Type used in API method '${serializedType.declaration.qualifiedName!!.asString()}' must be Serializable" }
    }

    private var invoked = false

    private fun <T> invokeOnce(orElse: () -> T, invocation: () -> T): T {
        return if (!invoked) {
            invoked = true
            invocation()
        } else orElse()
    }
}

