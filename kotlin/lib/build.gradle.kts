import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.readBytes

plugins {
    alias(libs.plugins.kotlinMultiplatform)
//    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.serialization)
    id("org.jetbrains.kotlinx.atomicfu") version "0.25.0"
}






val projGroup: String by project

group = projGroup
version = libs.versions.rpc4k.get()
base.archivesName = "rpc4k-runtime"



kotlin {

    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
    }
    explicitApi()
    targetHierarchy.default()
    jvm()
    wasmJs {
        browser()
    }
    iosArm64()
    jvmToolchain(17)
//    androidTarget {
//        publishLibraryVariants("release")
//        compilations.all {
//            kotlinOptions {
//                jvmTarget = "1.8"
//            }
//        }
//    }

    sourceSets {
        val jvmMain by getting {
            dependencies {
                //TODO: should be part of testing module
                api(libs.junit)
                //TODO: should be part of ktor server module
                api(libs.ktor.server.core.jvm)
                api(libs.ktor.server.websockets.jvm)
                api(libs.ktor.netty)
                api(libs.ktor.logging)
                //TODo: should be part of okhttp client module
                api(libs.okhttp.core)
            }
        }
        val commonMain by getting {
            dependencies {
                api(libs.coroutines.core)
                api(libs.serialization.json)
                api (libs.uuid)
                api(libs.kotlinx.datetime)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(Testing.Strikt.core)
                implementation(libs.logback)
            }
        }

        val nativeMain by getting {
            dependencies {
                // Used to fill in for java concurrency primitives
                implementation(libs.stately.concurrent.collections)
            }
        }

        val wasmJsMain by getting {
            dependencies {
                // Used to fill in for java concurrency primitives
                implementation(libs.stately.concurrent.collections)
            }
        }

    }
}


//
//android {
//    namespace = projGroup
//    compileSdk = libs.versions.android.compileSdk.get().toInt()
//    defaultConfig {
//        minSdk = libs.versions.android.minSdk.get().toInt()
//    }
//}