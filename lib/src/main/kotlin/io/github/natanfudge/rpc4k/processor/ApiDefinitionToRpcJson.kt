package io.github.natanfudge.rpc4k.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSFile
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


object ApiDefinitionToRpcJson {
    private val json = Json {
        prettyPrint = true
    }
    fun write(definition: ApiDefinition, codeGenerator: CodeGenerator, containingFile: KSFile) {
        codeGenerator.createNewFile(
            packageName = "", dependencies = Dependencies(aggregating = false, containingFile), fileName = "${definition.name}Api", extensionName = "json"
        ).use { it.write(json.encodeToString(definition).toByteArray())  }
    }
}