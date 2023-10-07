package io.github.natanfudge.rpc4k.test

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders
import io.github.natanfudge.rpc4k.processor.old.Rpc4kProcessorProviderOld
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals


class TestStuff {

    @Test
    fun `test my annotation processor`() {
        val testSources = File("../workload/src").walkBottomUp().toList()
            .filter { it.isFile }
            .map { SourceFile.fromPath(it) }

        val result = KotlinCompilation().apply {
            sources = testSources
            symbolProcessorProviders = listOf(Rpc4kProcessorProviderOld())
            inheritClassPath = true
            messageOutputStream = System.out // see diagnostics in real time
        }.compile()

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

    }

}