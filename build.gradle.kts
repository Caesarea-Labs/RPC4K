plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.serialization)
}
allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    repositories {
        mavenCentral()
        google()
    }

    kotlin {
        jvmToolchain(16)
        compilerOptions.freeCompilerArgs.add("-Xcontext-receivers")
    }
    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(16)
        }
    }
}