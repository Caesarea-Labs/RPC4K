plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.serialization)
    alias(libs.plugins.nexus.publish)
}
allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    repositories {
        mavenCentral()
        google()
    }

    kotlin {
        jvmToolchain(17)
        compilerOptions.freeCompilerArgs.add("-Xcontext-receivers")
    }
}


nexusPublishing {
    this@nexusPublishing.repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            stagingProfileId = System.getenv("CLABS_SONATYPE_STAGING_PROFILE_ID")
            username = System.getenv("CLABS_OSSRH_USERNAME")
            password = System.getenv("CLABS_OSSRH_PASSWORD")
        }
    }
}

project(":lib").afterEvaluate {
    tasks.create("uploadLibrary") {
        group = "upload"
        dependsOn(
            project(":lib").tasks["publishToSonatype"],
            rootProject.tasks["closeAndReleaseSonatypeStagingRepository"],
            gradle.includedBuild("plugin").task(":publishPlugins")
        )
    }
}
