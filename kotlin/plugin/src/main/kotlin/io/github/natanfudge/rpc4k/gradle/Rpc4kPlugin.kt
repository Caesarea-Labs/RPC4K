package io.github.natanfudge.rpc4k.gradle

import com.github.gradle.node.npm.task.NpxTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

class Rpc4KPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = with(project) {
        plugins.apply("com.google.devtools.ksp")

        val extension = extensions.create<Rpc4kExtension>("rpc4k")

        afterEvaluate {
            if (extension.dev) {
                dependencies.add("implementation", project(":lib"))
            } else {
                TODO("add ksp plugin and runtime")
            }
            println("Top after")

            if (extension.typescriptDir != null) {
                println("Dir after")
                plugins.apply("com.github.node-gradle.node")

                tasks.create<NpxTask>("generateTypescriptClient") {
                    val jsonPath = project.layout.buildDirectory.dir("generated/ksp/main/resources/rpc4k").get().asFile.absolutePath
                    val resultPath = toPath(extension.typescriptDir)!!.absolutePathString()
                    inputs.dir(jsonPath)
                    outputs.dir(resultPath)

                    val rpc4tsArgs = mutableListOf("-i$jsonPath", "-o$resultPath")
                    if (extension.dev) rpc4tsArgs.add("-d")

                    // NiceToHave: support multiple source sets
                    tasks.getByName("kspKotlin").finalizedBy(this)

                    if (extension.dev) {
                        command.set("ts-node")
                        val projectRoot =  project.rootDir.parentFile.resolve("typescript/src/")
                        inputs.dir(projectRoot)
                        val generatorPath = projectRoot.resolve("generator/GeneratorMain.ts").absolutePath
                        args.set(listOf(generatorPath) + rpc4tsArgs)
                    } else {
                        // TODO: add an input property that depends on the version of the rpc4ts- generator
                        TODO("invoke rpc4ts-generator")
                    }
                }
            }
        }

    }
}

open class Rpc4kExtension {
    /**
     * A path to a directory in a typescript project where the generated source should exist.
     */
    var typescriptDir: Any? = null
        set(value) {
            require(toPath(value) != null) { "typescriptDir value $value is not a file path" }
            field = value
        }

    /**
     * Should only be set to true in RPC4k itself
     */
    var dev = false
}

private fun toPath(value: Any?): Path? = when (value) {
    is File -> value.toPath()
    is Path -> value
    is String -> Paths.get(value)
    else -> null
}