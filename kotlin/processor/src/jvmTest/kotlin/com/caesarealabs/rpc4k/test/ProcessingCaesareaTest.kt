@file:OptIn(ExperimentalCompilerApi::class)

package com.caesarealabs.rpc4k.test

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders
import com.caesarealabs.rpc4k.processor.Rpc4kProcessorProvider
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import strikt.api.expectThat
import strikt.assertions.isNotEmpty
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals


class ProcessingCaesareaTest {

    @Test
    fun `Process CaesareaPoC`() {
        println("path: " + System.getProperty("user.dir"))
        val testSources = File("../../../poc/server/src").walkBottomUp()
            .filter { it.isFile && it.extension == "kt" }
            .map { SourceFile.fromPath(it) }
            .toList()

        expectThat(testSources).isNotEmpty()

        val result = KotlinCompilation().apply {
            sources = testSources
            symbolProcessorProviders = listOf(com.caesarealabs.rpc4k.processor.Rpc4kProcessorProvider())
            inheritClassPath = true
            messageOutputStream = System.out // see diagnostics in real time
        }.compile()

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

    }

}