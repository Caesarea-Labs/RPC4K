plugins {
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
//    alias(libs.plugins.serialization)
    alias(libs.plugins.nexus.publish)
    id("signing")
    id("maven-publish")
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

//project(":lib").afterEvaluate {
//    tasks.create("uploadLibrary") {
//        group = "upload"
//        dependsOn(
//            project(":lib").tasks["publishToSonatype"],
//            rootProject.tasks["closeAndReleaseSonatypeStagingRepository"],
//            gradle.includedBuild("plugin").task(":publishPlugins")
//        )
//    }
//}
val projectGroup = "com.caesarealabs"
val projectId = "rpc4k"
val projectVersion = libs.versions.rpc4k.get()
val githubUrl = "https://github.com/natanfudge/Rpc4k"
val projectLicense = "The MIT License"

publishing {
    publications.withType<MavenPublication> {
//            register("release", MavenPublication::class) {
        // Stub javadoc.jar artifact to appease Maven Central
        artifact(tasks.register("${name}JavadocJar", Jar::class) {
            archiveClassifier.set("javadoc")
            archiveAppendix.set("release")
        })
//
//                // we'll set up later
//                groupId = projectGroup
//                artifactId = projectId
//                version = projectVersion
//
//                from(components["java"])

        // Mostly self-explanatory metadata
        pom {
            name = projectId
            description = "RPC Utility"
            url = githubUrl
            licenses {
                license {
                    name = projectLicense
                }
            }
            developers {
                developer {
                    id = "natan"
                    name = "Natan Lifshitz"
                    email = "natan.lifshitz@caesarealabs.com"
                }
            }

            scm {
                url = githubUrl
            }
        }
    }
}

// Secrets are set in github actions

signing {
    useInMemoryPgpKeys(
        System.getenv("CLABS_GPG_KEY_ID"),
        // The ONELINE secret key contains literal '\n' in place of new lines in order to make it fit in a windows env variable.
        // We then replace the literal '\n's with actual '\n's.
        System.getenv("CLABS_GPG_SECRET_KEY_ONELINE")?.replace("\\n", "\n"),
        System.getenv("CLABS_GPG_KEY_PASSWORD")
    )
    sign(publishing.publications)
}
