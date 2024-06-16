import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.readBytes

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
}






val projGroup: String by project

group = projGroup
version = libs.versions.rpc4k.get()


kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
    }
    jvm()


    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(libs.symbol.processing.api)
                implementation(libs.kotlinpoet.core)
                implementation(libs.kotlinpoet.ksp)
                implementation(libs.junit)
                implementation(project(":lib"))
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.compile.testing.ksp)
                implementation(Testing.Strikt.core)
                implementation(libs.logback)
            }
        }
    }
}

