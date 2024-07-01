import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.readBytes

plugins {
    alias(libs.plugins.kotlinMultiplatform)
//    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.serialization)
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
                implementation(libs.junit)
                implementation(libs.ktor.server.core.jvm)
                implementation(libs.ktor.server.websockets.jvm)
                implementation(libs.ktor.netty)
                implementation(libs.ktor.logging)
                implementation(libs.okhttp.core)
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