package io.github.natanfudge.rpc4k.test

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders
import io.github.natanfudge.rpc4k.processor.Rpc4kProcessorProvider
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals


class DirectProcessorTest {

    @Test
    fun `Symbol processor success`() {
        val testSources = (File("../testapp/src/main").walkBottomUp() + File("../testapp/src/test").walkBottomUp())
            .filter { it.isFile }
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
        for(errorFile in File("../testapp/src/errors").walkBottomUp().filter { it.isFile }) {
            val testSources = listOf(SourceFile.fromPath(errorFile))

            val result = KotlinCompilation().apply {
                sources = testSources
                symbolProcessorProviders = listOf(Rpc4kProcessorProvider())
                inheritClassPath = true
                messageOutputStream = System.out // see diagnostics in real time
            }.compile()

            assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        }
    }

}