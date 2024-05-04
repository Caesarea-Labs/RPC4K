package com.caesarealabs.rpc4k.processor

import com.caesarealabs.rpc4k.processor.utils.checkRequirement
import com.caesarealabs.rpc4k.processor.utils.findDuplicate
import com.caesarealabs.rpc4k.processor.utils.getClassesWithAnnotation
import com.caesarealabs.rpc4k.processor.utils.getQualifiedName
import com.caesarealabs.rpc4k.runtime.user.Api
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ksp.writeTo
import kotlin.system.measureTimeMillis

internal class Rpc4kProcessorProvider : SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment
    ): SymbolProcessor = Rpc4kProcessor(environment)
}

internal class Rpc4kProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {
    private var processed = false

    override fun process(resolver: Resolver): List<KSAnnotated> = with(env) {
        if (processed) return listOf()
        processed = true
        val validator = ApiClassValidator(env, resolver)


        val time = measureTimeMillis {
            val apiClasses = resolver.getClassesWithAnnotation(Api::class)
                .filter { validator.validate(it) }
                .toHashSet()

            for (symbol in apiClasses) {
                generateRpc(symbol, resolver)
            }
        }
        env.logger.warn("Generating RPC Classes took ${time}ms")

        // Nothing needs to be deferred
        return listOf()
    }


    context(SymbolProcessorEnvironment)
    private fun generateRpc(apiClass: KSClassDeclaration, resolver: Resolver) {
        val time = measureTimeMillis {
            val api = KspToApiDefinition(resolver).toApiDefinition(apiClass)
            if (api == null) {
                env.logger.warn("Source code is invalid, code won't be generated for ${apiClass.getQualifiedName()}.")
                return
            }

            val nameDuplicate = api.models.findDuplicate { it.name.substringAfterLast(".") }

            // Since models are not namespaced, they cannot contain duplicate names
            apiClass.checkRequirement(env, nameDuplicate == null) { "There's two types with the name '$nameDuplicate', which is not allowed" }
            if (nameDuplicate != null) return

            val file = apiClass.containingFile!!
            val files = listOf(file)
//            if (apiClass.shouldGenerateClient()) {
                ApiDefinitionToClientCode.convert(api).writeTo(codeGenerator, false, files)
//            }
            ApiDefinitionToServerCode(api).convert().writeTo(codeGenerator, false, files)
            ApiDefinitionWriter.writeRpcJsons(api, codeGenerator, file)
        }

        env.logger.warn("Generated RPC classes for: ${apiClass.qualifiedName!!.asString()} in $time millis")
    }

}

internal fun KSClassDeclaration.shouldGenerateClient(): Boolean {
    // Checks if the @Api annotation has the only argument (generateClient) set to true
    return annotations.first { it.shortName.asString() == Api::class.simpleName }.arguments[0].value == true
}