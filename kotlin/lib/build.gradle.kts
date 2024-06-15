import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.readBytes

plugins {
    id("signing")
    id("maven-publish")
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.serialization)
}





fun rpc4kRuntimeVersion() = libs.versions.rpc4k.get()

val projectGroup = "com.caesarealabs"
val projectId = "rpc4k"
val projectVersion = rpc4kRuntimeVersion()
val githubUrl = "https://github.com/natanfudge/Rpc4k"
val projectLicense = "The MIT License"

group = projectGroup
version = projectVersion




//tasks.test {
//    useJUnitPlatform()
//    testLogging {
//        events("passed", "skipped", "failed", "standardOut", "standardError")
//    }
//}

//sourceSets.main {
//    java.srcDirs("src/main/kotlin")
//}


afterEvaluate {
    publishing {
        publications.withType<MavenPublication> {
//            register("release", MavenPublication::class) {
                // Stub javadoc.jar artifact to appease Maven Central
                artifact(tasks.register("${name}JavadocJar", Jar::class) {
                    archiveClassifier.set("javadoc")
                    archiveAppendix.set("release")
                })
//
//                // The coordinates of the library, being set from variables that
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
//        }
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

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
    }
    explicitApi()
    targetHierarchy.default()
    jvm()
    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.symbol.processing.api)
                implementation(libs.kotlinpoet.core)
                implementation(libs.kotlinpoet.ksp)
                implementation(libs.junit)

                api(libs.coroutines.core)
                api(libs.serialization.json)

                implementation(libs.okhttp.core)
                implementation(libs.okhttp.sse)
                implementation(libs.ktor.netty)
                implementation(libs.ktor.logging)
                implementation("io.ktor:ktor-server-core-jvm:2.2.4")
                implementation("io.ktor:ktor-server-websockets-jvm:2.2.4")

            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.compile.testing.ksp)
                implementation(Testing.Strikt.core)
                implementation(libs.logback)
            }
        }
    }
}

android {
    namespace = projectGroup
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}