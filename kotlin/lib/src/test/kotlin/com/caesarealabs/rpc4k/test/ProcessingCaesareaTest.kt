@file:OptIn(ExperimentalCompilerApi::class)

package com.caesarealabs.rpc4k.test

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders
import com.caesarealabs.rpc4k.processor.Rpc4kProcessorProvider
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals


class ProcessingCaesareaTest {

    @Test
    fun `Process CaesareaPoC`() {
        val testSources = File("../../../work/CaesareaPoC/server/src").walkBottomUp()
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

}