package io.github.natanfudge.rpc4k.test

import com.example.UserProtocol
import io.github.natanfudge.rpc4k.typescript.TypescriptModelGenerator
import java.nio.file.Paths
import kotlin.test.Test

class ModelToTypescriptTest {
    @Test
    fun `Model to typescript conversion succeeds`() {
        val models = ModelToTypescriptTest::class.java.getResourceAsStream("/UserProtocol.Models.txt")!!.readAllBytes().decodeToString()
        TypescriptModelGenerator.generate(models, Paths.get(""))
    }
}