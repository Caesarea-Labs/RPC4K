@file:Suppress("OPT_IN_USAGE")

plugins {
    alias(libs.plugins.kotlinMultiplatform)
//    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.serialization)
    id("org.jetbrains.kotlinx.atomicfu") version "0.25.0"
    kotlin("plugin.power-assert") version "2.0.20-Beta2"
}






val projGroup: String by project

group = projGroup
version = libs.versions.rpc4k.get()
base.archivesName = "rpc4k-runtime"



kotlin {

//    compilerOptions {
//        freeCompilerArgs.add("-Xcontext-receivers")
//    }
    explicitApi()
//    targetHierarchy.default()
    jvm()
    wasmJs {
        browser()
    }
//    iosArm64()
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
                //NiceToHave: should be part of testing module
                api(libs.junit)
                implementation(libs.mongodb.testcontainer)
                // Atomicfu doesn't properly add this dependency to dependants with just the plugin for some reason
                implementation("org.jetbrains.kotlinx:atomicfu:0.25.0")
                //NiceToHave: should be part of ktor server module
                api(libs.ktor.server.core.jvm)
                api(libs.ktor.server.websockets.jvm)
                api(libs.ktor.netty)
                api(libs.ktor.logging)
                implementation("ch.qos.logback:logback-classic:1.5.6")
                //NiceToHave: should be part of okhttp client module
                api(libs.okhttp.core)

                // NiceToHave: should be part of ktor client module
                // Use okhttp client on JVM
                api(libs.ktor.client.okhttp)

                // NiceToHave: should be part of aws server module
                api(libs.aws.lambda.handler.core)
                api(libs.aws.lambda.handler.events)
                api(libs.aws.gateway)

                // NiceToHave: should be part of mongodb event manager module
                api(libs.mongodb.kotlin)
                api(libs.mongodb.serialization)
            }
        }
        val commonMain by getting {
            dependencies {
                api(libs.coroutines.core)
                api(libs.serialization.json)
                api (libs.uuid)
                api(libs.kotlinx.datetime)
                api(libs.logging)

                // NiceToHave: should be part of ktor client module
                api(libs.ktor.client.core)
                implementation(libs.ktor.client.websockets)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(Testing.Strikt.core)
                implementation(libs.logback)
            }
        }

//        val nativeMain by getting {
//            dependencies {
//                // Used to fill in for java concurrency primitives
//                implementation(libs.stately.concurrent.collections)
//            }
//        }

        val wasmJsMain by getting {
            dependencies {
                // Used to fill in for java concurrency primitives
                implementation(libs.stately.concurrent.collections)

                // NICETOHAVE: should be part of ktor client module
                // Use js client on wasmJs
                api(libs.ktor.client.js)
            }
        }

    }
}
powerAssert {
    functions = listOf("kotlin.test.assertTrue", "kotlin.test.assertEquals", "kotlin.test.assertContentEquals")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

//
//android {
//    namespace = projGroup
//    compileSdk = libs.versions.android.compileSdk.get().toInt()
//    defaultConfig {
//        minSdk = libs.versions.android.minSdk.get().toInt()
//    }
//}