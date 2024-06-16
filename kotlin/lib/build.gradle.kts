import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.readBytes

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.serialization)
}






val projGroup: String by project

group = projGroup
version = libs.versions.rpc4k.get()




kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
    }
    explicitApi()
    targetHierarchy.default()
    jvm()
    wasmJs()
    iosArm64()
    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(libs.junit)
                implementation(libs.ktor.server.core.jvm)
                implementation(libs.ktor.server.websockets.jvm)
                implementation(libs.ktor.netty)
            }
        }
        val commonMain by getting {
            dependencies {
                api(libs.coroutines.core)
                api(libs.serialization.json)

                implementation(libs.okhttp.core)
                implementation(libs.ktor.logging)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(Testing.Strikt.core)
                implementation(libs.logback)
            }
        }
    }
}

android {
    namespace = projGroup
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}