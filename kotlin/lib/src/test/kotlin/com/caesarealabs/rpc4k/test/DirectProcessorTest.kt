package com.caesarealabs.rpc4k.test

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders
import com.caesarealabs.rpc4k.processor.Rpc4kProcessorProvider
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals


class DirectProcessorTest {

    @Test
    fun `Symbol processor success`() {
        val testSources = (File("../testapp/src/main").walkBottomUp() + File("../testapp/src/test").walkBottomUp())
            .filter { it.isFile && it.extension == "kt" }
            .map { SourceFile.fromPath(it) }
            .toList()

        val result = KotlinCompilation().apply {
            sources = testSources
            symbolProcessorProviders = listOf(Rpc4kProcessorProvider())
            inheritClassPath = true
            messageOutputStream = System.out // see diagnostics in real time
        }.compile()

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

    }

    @Test
    fun `Symbol processor gives off correct errors`() {
        val errorCasesDir = File("../testerrors")
        for (errorFile in errorCasesDir.listFiles()!!.filter { it.isFile }) {
            // Test individual files
            val testSources = listOf(SourceFile.fromPath(errorFile))

            val result = KotlinCompilation().apply {
                sources = testSources
                symbolProcessorProviders = listOf(Rpc4kProcessorProvider())
                inheritClassPath = true
                messageOutputStream = System.out // see diagnostics in real time
            }.compile()

            expectThat(result.exitCode)
                .describedAs { "Exit code for error file $errorFile" }
                .isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        }

        // Test DuplicateTypeName together with 2 other files
        val testSources = listOf(
            errorCasesDir.sourceFile("special/DuplicateTypeName"),
            errorCasesDir.sourceFile("package1/Foo"),
            errorCasesDir.sourceFile("package2/Foo"),
        )

        val result = KotlinCompilation().apply {
            sources = testSources
            symbolProcessorProviders = listOf(Rpc4kProcessorProvider())
            inheritClassPath = true
            messageOutputStream = System.out // see diagnostics in real time
        }.compile()

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
    }

    private fun File.sourceFile(path: String) = SourceFile.fromPath(resolve("$path.kt"))

}