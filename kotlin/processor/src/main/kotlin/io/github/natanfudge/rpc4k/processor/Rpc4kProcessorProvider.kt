package io.github.natanfudge.rpc4k.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import io.github.natanfudge.rpc4k.processor.utils.getClassesWithAnnotation
import io.github.natanfudge.rpc4k.processor.utils.poet.companion
import io.github.natanfudge.rpc4k.runtime.api.ApiClient
import io.github.natanfudge.rpc4k.runtime.api.ApiServer
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

        env.logger.info("Processing @Api")
        val validator = ApiClassValidator(env)
        val time = measureTimeMillis {
            val apiClients = resolver.getClassesWithAnnotation(ApiClient::class)
                .filter { it.validate() && validator.checkApiClassIsValid(it) && validator.checkClassIsSuspendOpen(it) }
                .toHashSet()
            // Servers don't need to be suspendOpen
            val apiServers = resolver.getClassesWithAnnotation(ApiServer::class)
                .filter { it.validate() && validator.checkApiClassIsValid(it)  }
                .toHashSet()
            val apiClasses = (apiClients + apiServers).toSet() // Get rid of duplicates
            for (symbol in apiClasses) {
                // We try to only call generateRpc once because KspToApiDefinition.convert() is expensive
                generateRpc(symbol, client = symbol in apiClients, server = symbol in apiServers)
            }
        }
        env.logger.warn("Generating RPC Classes took ${time}ms")

        // Nothing needs to be deferred
        return listOf()
    }

    context(SymbolProcessorEnvironment)
    private fun generateRpc(apiClass: KSClassDeclaration, client: Boolean, server: Boolean) {
        val time = measureTimeMillis {
            val className = apiClass.toClassName()
            val api = KspToApiDefinition.convert(apiClass)
            val context = JvmContext(
                userClassName = className,
                userCompanionClassName = className.companion(),
                userClassIsInterface = apiClass.classKind == ClassKind.INTERFACE
            )
            with(context) {
                if (client) ApiDefinitionToClientCode.convert(api).writeTo(codeGenerator, false, listOf(apiClass.containingFile!!))
                if (server) ApiDefinitionToServerCode.convert(api).writeTo(codeGenerator, false, listOf(apiClass.containingFile!!))
            }
        }

        env.logger.warn("Generated RPC classes for: ${apiClass.qualifiedName!!.asString()} in $time millis")
    }

}

