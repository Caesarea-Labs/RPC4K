package com.caesarealabs.rpc4k.gradle

import com.github.gradle.node.npm.task.NpxTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolutePathString


private const val DevGeneratorDir = "typescript/generator/src/"
private const val DevGeneratorMain = "index.ts"

class Rpc4KPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = with(project) {
        plugins.apply("com.google.devtools.ksp")

        // System.setProperty("rpc4k.dev", "true") is invoked in the dev project to know when to use local paths
        // This is a system property instead of an extension configuration because we need this information EARLY.
        val dev = System.getProperty("rpc4k.dev") == "true"

        val lib = if (dev) project(":lib") else "com.caesarealabs:rpc4k-runtime:${getRpc4kVersion()}"
        val processor = if (dev) project(":processor") else "com.caesarealabs:rpc4k-processor:${getRpc4kVersion()}"
        val extension = extensions.create<Rpc4kExtension>("rpc4k")
        val jvm = plugins.hasPlugin("org.jetbrains.kotlin.jvm")

        if (jvm) {
            // Kotlin-JVM logic
            // Depend on runtime
            dependencies.add("implementation", lib)
            // Apply KSP processor
            dependencies.add("ksp", processor)
        } else if (plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
            println("RPC4K will be applied to commonMain ONLY!")
            val kmp = extensions.getByType<KotlinMultiplatformExtension>()
            // KSP doesn't know to detect this by itself
            kmp.sourceSets.named("commonMain").configure {
                kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            }
            // Do in afterEvaluate because the multiplatform compile tasks are created after user configuration
            afterEvaluate {
                // KSP doesn't know to make this task run by itself
                tasks.withType<AbstractKotlinCompile<*>> {
                    if (name != "kspCommonMainKotlinMetadata") {
                        dependsOn("kspCommonMainKotlinMetadata")
                    }
                }
            }


            // Apply KSP processor
            dependencies.add("kspCommonMainMetadata", processor)

            // Depend on runtime
            kmp.sourceSets["commonMain"].dependencies {
                implementation(lib)
            }
        } else {
            throw IllegalStateException("No Kotlin plugin detected, make sure to apply it before RPC4K. ")
        }

        afterEvaluate {
            val generatedSourcesPath = if (jvm) "main" else "metadata/commonMain"
            val kspTask = if (jvm) "kspKotlin" else "kspCommonMainKotlinMetadata"
            if (extension.typescriptDir != null) {
                plugins.apply("com.github.node-gradle.node")

                tasks.create<NpxTask>("generateTypescriptClient") {
                    dependsOn(kspTask)
                    // This is the only reliable way i have found to make this task run in multiplatform
                    tasks.withType<KotlinCompile> {
                        finalizedBy(this@create)
                    }

                    // NiceToHave: support multiple source sets
                    val jsonPath =
                        project.layout.buildDirectory.dir("generated/ksp/$generatedSourcesPath/resources/rpc4k").get().asFile.absolutePath
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