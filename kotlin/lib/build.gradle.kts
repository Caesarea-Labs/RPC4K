import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.readBytes

plugins {
    id("signing")
    id("maven-publish")
}

fun rpc4kRuntimeVersion() = libs.versions.rpc4k.get()

val projectGroup = "com.caesarealabs"
val projectId = "rpc4k"
val projectVersion = rpc4kRuntimeVersion()
val githubUrl = "https://github.com/natanfudge/Rpc4k"
val projectLicense = "The MIT License"

group = projectGroup
version = projectVersion


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

    testImplementation(libs.kotlin.test)
    testImplementation(libs.compile.testing.ksp)
    testImplementation(Testing.Strikt.core)
    testImplementation(libs.logback)

}

java {
    withSourcesJar()
    withJavadocJar()
}

kotlin {
    explicitApi()
}


tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed", "standardOut", "standardError")
    }
}

sourceSets.main {
    java.srcDirs("src/main/kotlin")
}


afterEvaluate {
    publishing {
        publications {
            register("release", MavenPublication::class) {
                // The coordinates of the library, being set from variables that
                // we'll set up later
                groupId = projectGroup
                artifactId = projectId
                version = projectVersion

                from(components["java"])

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
