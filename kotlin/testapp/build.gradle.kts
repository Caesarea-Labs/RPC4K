buildscript {
    // Sets up the plugin with local paths
    System.setProperty("rpc4k.dev", "true")
}
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    id("com.caesarealabs.rpc4k")
}

kotlin {
    jvmToolchain(21)
    compilerOptions.freeCompilerArgs.add("-Xcontext-receivers")
}

rpc4k {
    typescriptDir = rootDir.parentFile.resolve("typescript/runtime/test/generated")
}



version = "1.0-SNAPSHOT"

//tasks.test {
//    useJUnitPlatform()
//    testLogging {
//        events("passed", "skipped", "failed")
//        showStandardStreams = true
//        showStackTraces = true
//        showExceptions = true
//        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
//    }
//}

kotlin {
    jvm()
    sourceSets {
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
            }
        }
    }
}

dependencies {
    add("kspJvm", project(":lib"))
    add("kspJvmTest", project(":lib"))
}

//TODO: Road to Getting RPC4K to Multiplatform:
// 1. Resolve test failing (I think it failed before, I just didn't run it)
// 2. Adapt Gradle Plugin to KMP
// 3. Properly set up publishing for KMP
// 4. Test on non-jvm app