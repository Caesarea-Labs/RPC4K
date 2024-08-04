import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

buildscript {
    // Sets up the plugin with local paths
//    System.setProperty("rpc4k.dev", "true")
}
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    id("com.caesarealabs.rpc4k")
}


kotlin {
    jvmToolchain(21)
//    compilerOptions.freeCompilerArgs.add("-Xcontext-receivers")
}

rpc4k {
    typescriptDir = rootDir.parentFile.resolve("typescript/runtime/test/generated")
}



version = "1.0-SNAPSHOT"


kotlin {
    jvm()
    // Must be added for KSP to work in common
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

//    iosArm64()
    sourceSets {
        commonMain.dependencies {
            implementation(libs.logging)
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.strikt)
                implementation(libs.okhttp.core)
                implementation(libs.ktor.netty)
                implementation(libs.logback)
                implementation ("org.junit.jupiter:junit-jupiter-api:5.8.1")
//                implementation ("org.junit.jupiter:junit-jupiter-params:5.8.1")
                implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0-RC")
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

