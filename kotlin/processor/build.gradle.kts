plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
//    id("maven-publish")
}


val projGroup: String by project

group = projGroup
base.archivesName = "rpc4k-processor"
version = libs.versions.rpc4k.get()

//region Fix Gradle warning about signing tasks using publishing task outputs without explicit dependencies
// https://github.com/gradle/gradle/issues/26091
tasks.withType<AbstractPublishToMaven>().configureEach {
    val signingTasks = tasks.withType<Sign>()
    mustRunAfter(signingTasks)
}
//endregion




kotlin {
//    compilerOptions {
//        freeCompilerArgs.add("-Xcontext-receivers")
//    }
    jvm()
    jvmToolchain(17)


    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(libs.symbol.processing.api)
                implementation(libs.kotlinpoet.core)
                implementation(libs.kotlinpoet.ksp)
                implementation(libs.junit)
                compileOnly(libs.kotlinx.datetime)
                compileOnly(libs.uuid)
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

