plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.serialization)
    alias (libs.plugins.nexus.publish)
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


nexusPublishing {
    this@nexusPublishing.repositories {
        sonatype {
            stagingProfileId = System.getenv("SONATYPE_STAGING_PROFILE_ID")
            username = System.getenv("OSSRH_USERNAME")
            password = System.getenv("OSSRH_PASSWORD")
        }
    }
}

project(":lib").afterEvaluate {
    tasks.create("uploadLibrary") {
        group = "upload"
        dependsOn(project(":lib").tasks["publishToSonatype"], rootProject.tasks["closeAndReleaseSonatypeStagingRepository"])
    }
}
//afterEvaluate {
//    tasks.create("uploadLibrary") {
//        group = "upload"
//        dependsOn(rootProject.tasks["publishToSonatype"], rootProject.tasks["closeAndReleaseSonatypeStagingRepository"])
//    }
//}