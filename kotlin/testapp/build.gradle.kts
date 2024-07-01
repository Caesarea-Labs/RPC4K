
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


kotlin {
    jvm()
    // Must be added for KSP to work in common
    wasmJs {
        browser()
    }

    iosArm64()
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

tasks.withType<Test> {
    useJUnitPlatform()
}


//TODO: Road to Getting RPC4K to Multiplatform:
// 3. Properly set up publishing for KMP
// 3.5 Find, use and test a multiplatform server and client to be used as a default
// 4. Test on non-jvm app