plugins {
    alias(libs.plugins.ksp)
    id("io.github.natanfudge.rpc4k")
}

rpc4k {
    dev = true
    typescriptDir = rootDir.parentFile.resolve("typescript/runtime/test/generated")
}

version = "1.0-SNAPSHOT"

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        showStackTraces = true
        showExceptions = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}


dependencies {
    implementation(kotlin("stdlib"))
    "ksp"(project(":lib"))
    testImplementation(kotlin("test"))
    testImplementation(Testing.Strikt.core)
    testImplementation(libs.okhttp.core)
    testImplementation(libs.ktor.netty)
    testImplementation(libs.logback)
}


