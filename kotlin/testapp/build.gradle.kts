buildscript {
    // Sets up the plugin with local paths
    System.setProperty("rpc4k.dev", "true")
}
plugins {
    id("io.github.natanfudge.rpc4k")
}

rpc4k {
    typescriptDir = rootDir.parentFile.resolve("typescript/runtime/src/generated")
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
    testImplementation(kotlin("test"))
    testImplementation(libs.strikt)
    testImplementation(libs.okhttp.core)
    testImplementation(libs.ktor.netty)
    testImplementation(libs.logback)
}


