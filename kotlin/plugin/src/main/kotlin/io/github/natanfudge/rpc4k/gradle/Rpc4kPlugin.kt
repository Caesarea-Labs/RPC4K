package io.github.natanfudge.rpc4k.gradle

import com.github.gradle.node.npm.task.NpxTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolutePathString


private const val DevGeneratorDir = "typescript/generator/src/"
private const val DevGeneratorMain = "index.ts"

class Rpc4KPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = with(project) {
        plugins.apply("com.google.devtools.ksp")

//        System.setProperty("rpc4k.dev", "true") is invoke in the dev project to know when to use local paths
        val dev = System.getProperty("rpc4k.dev") == "true"

        val artifact = if (dev) project(":lib") else "io.github.natanfudge:rpc4k:${getRpc4kVersion()}"

        // Depend on runtime
        dependencies.add("implementation", artifact)
        // Apply KSP processor
        dependencies.add("ksp", artifact)


        val extension = extensions.create<Rpc4kExtension>("rpc4k")

        afterEvaluate {
            if (extension.typescriptDir != null) {
                plugins.apply("com.github.node-gradle.node")

                tasks.create<NpxTask>("generateTypescriptClient") {
                    dependsOn("kspKotlin")
                    tasks["classes"].dependsOn(this)

                    // NiceToHave: support multiple source sets
                    val jsonPath = project.layout.buildDirectory.dir("generated/ksp/main/resources/rpc4k").get().asFile.absolutePath
                    val resultPath = toPath(extension.typescriptDir)!!.absolutePathString()
                    inputs.dir(jsonPath)
                    outputs.dir(resultPath)
                    inputs.properties(mapOf("dev" to dev, "typescriptDir" to extension.typescriptDir))

                    val rpc4tsArgs = mutableListOf("-i$jsonPath", "-o$resultPath")
                    if (dev) rpc4tsArgs.add("-d")

                    if (dev) {
                        command.set("ts-node")
                        val projectRoot = project.rootDir.parentFile.resolve(DevGeneratorDir)
                        inputs.dir(projectRoot)
                        val generatorPath = projectRoot.resolve(DevGeneratorMain).absolutePath
                        println("Running $rpc4tsArgs")
                        args.set(listOf(generatorPath) + rpc4tsArgs)
                    } else {
                        command.set("rpc4ts-gen")
                        args.set(rpc4tsArgs)
                    }
                }
            }
        }
    }
}


open class Rpc4kExtension {
    /**
     * You may set this value to generate the generateTypescriptClient task that allows generating typescript sources from
     * annotated @Api classes.
     * Set this to a path to a directory in a typescript project where the generated source should exist.
     */
    var typescriptDir: Any? = null
        set(value) {
            require(toPath(value) != null) { "typescriptDir value $value is not a file path" }
            field = value
        }

}

private fun toPath(value: Any?): Path? = when (value) {
    is File -> value.toPath()
    is Path -> value
    is String -> Paths.get(value)
    else -> null
}


private fun getRpc4kVersion() = Rpc4KPlugin::class.java.getResourceAsStream("/rpc4k_version.txt")!!.readAllBytes().decodeToString()