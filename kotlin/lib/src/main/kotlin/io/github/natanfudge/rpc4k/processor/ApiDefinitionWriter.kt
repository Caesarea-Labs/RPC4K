package io.github.natanfudge.rpc4k.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSFile
import io.github.natanfudge.rpc4k.processor.utils.writeFile
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


object ApiDefinitionWriter {
    private val json = Json {
        prettyPrint = true
    }

    fun writeRpcJsons(definition: ApiDefinition, codeGenerator: CodeGenerator, containingFile: KSFile) {
        val models = json.encodeToString(definition)
        codeGenerator.writeFile(models, Dependencies(aggregating = false, containingFile), "rpc4k/" + definition.name.simple,
            extensionName = "rpc.json"
        )
    }
}
