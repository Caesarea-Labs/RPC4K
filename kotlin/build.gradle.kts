plugins {
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
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

//TODO: next step: evolve from maven local to sonatype. Current problem: could not PUT JVM publication.


nexusPublishing {
    this@nexusPublishing.repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            stagingProfileId = System.getenv("CLABS_SONATYPE_STAGING_PROFILE_ID")
            username = System.getenv("CLABS_OSSRH_USERTOKEN_USERNAME")
            password = System.getenv("CLABS_OSSRH_USERTOKEN_PASSWORD")

        }
    }
}

afterEvaluate {
    tasks.create("uploadLibrary") {
        group = "upload"
        dependsOn(
            // Publish KSP Processor
            project(":processor").tasks["publish"],
            // Publish Runtime
            project(":lib").tasks["publish"],
            // Publish Gradle Plugin
            gradle.includedBuild("plugin").task(":publishPlugins")
        )
        // Finally, release processor + runtime
        finalizedBy(rootProject.tasks["closeAndReleaseSonatypeStagingRepository"])
    }
}



subprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    // AfterEvaluate so we catch the various platform publications as well
    afterEvaluate {
        signing {
            useInMemoryPgpKeys(
                System.getenv("CLABS_GPG_KEY_ID"),
//         The ONELINE secret key contains literal '\n' in place of new lines in order to make it fit in a windows env variable.
//         We then replace the literal '\n's with actual '\n's.
                System.getenv("CLABS_GPG_SECRET_KEY_ONELINE")?.replace("\\n", "\n"),
                System.getenv("CLABS_GPG_KEY_PASSWORD")
            )
            afterEvaluate {
                sign(publishing.publications)
            }
        }
        publishing {
            publications.withType<MavenPublication> {
                val githubUrl = "https://github.com/natanfudge/Rpc4k"
                val projectLicense = "The MIT License"
                val prefix = when (project.name) {
                    "lib" -> "rpc4k-runtime"
                    "processor" -> "rpc4k-processor"
                    "testapp" -> "testapp"
                    else -> error("Unknown project: ${project.name}")
                }
                val suffix = if (name == "kotlinMultiplatform") "" else "-$name"
                artifactId = "$prefix$suffix"

                // Stub javadoc.jar artifact to appease Maven Central
                artifact(tasks.register("${name}JavadocJar", Jar::class) {
                    archiveClassifier.set("javadoc")
                    archiveAppendix.set("release")
                    archiveBaseName.set("${archiveBaseName.get()}-${name}")
                })

                // Mostly self-explanatory metadata
                pom {
                    name = artifactId
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
    }

}

