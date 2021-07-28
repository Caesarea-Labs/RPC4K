import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders
import io.github.natanfudge.rpc4k.Rpc4kProcessorProvider
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class TestStuff {
    class TestEnvClass {}

    @Test
    fun `test my annotation processor`() {
        println(File("").absolutePath)
        val testSources = File("../workload/src/main").walkBottomUp().toList()
            .filter { it.isFile }
            .map { SourceFile.fromPath(it) }

//        println("Alo")
//        val kotlinSource = SourceFile. SourceFile.kotlin(
//            "KClass.kt", """
//         @io.github.natanfudge.rpc4k.Api
//        class KClass {
//            fun foo() {
//                // Classes from the test environment are visible to the compiled sources
//                val testEnvClass = TestEnvClass()
//            }
//        }
//    """
//        )
//
        val result = KotlinCompilation().apply {
            sources = testSources
            symbolProcessorProviders = listOf(Rpc4kProcessorProvider())
            inheritClassPath = true
            messageOutputStream = System.out // see diagnostics in real time
        }.compile()

        assertEquals(KotlinCompilation.ExitCode.OK,result.exitCode)
    }
}