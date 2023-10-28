import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.readBytes

plugins {
    id("signing")
    id("maven-publish")
}

fun rpc4kRuntimeVersion() = libs.versions.rpc4k.runtime.get()

val projectGroup = "io.github.natanfudge"
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

    testImplementation(libs.kotlin.test)
    testImplementation(libs.compile.testing.ksp)
    testImplementation(Testing.Strikt.core)
    testImplementation(libs.logback)

}

java {
    withSourcesJar()
    withJavadocJar()
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



fun getSecretsDir() = System.getenv("SECRETS_PATH")
    ?: error("Missing SECRETS_PATH environment variables. Make sure to set the environment variable to the directory containing secrets.")

fun getSecretsFile(path: String): Path {
    val file = Paths.get(getSecretsDir(), path)
    if (!file.exists()) error("Missing secrets file $file. Make sure to create the file with the secrets.")
    return file
}

fun getSecretFileContents(path: String): String {
    return String(getSecretsFile(path).readBytes())
}



afterEvaluate {
    publishing {
        publications {
            register("release", MavenPublication::class) {
                groupId = projectGroup
                artifactId = projectId
                version = projectVersion

                from(components["java"])

                pom {
                    name = projectId
                    description = "Networking Utility"
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
                            email = "natan.lifsiz@gmail.com"
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

signing {
    useInMemoryPgpKeys(
        System.getenv("GPG_KEY_ID"), getSecretFileContents("ksm/secret_key.txt"), System.getenv("GPG_KEY_PASSWORD")
    )
    sign(publishing.publications)
}

