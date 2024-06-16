plugins {
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
//    alias(libs.plugins.serialization)
    alias(libs.plugins.nexus.publish)
}

allprojects {
    repositories {
        mavenCentral()
        google()
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
