import java.net.BindException
import java.net.ServerSocket

plugins {
    alias(libs.plugins.ksp)
    id("io.github.natanfudge.rpc4k")
}

rpc4k {
    dev = true
    typescriptDir = rootDir.parentFile.resolve("typescript/test/generated")
}

version = "1.0-SNAPSHOT"

tasks.test {
    useJUnitPlatform()
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


afterEvaluate {
    val startUserProtocolServer by tasks.creating(Test::class) {
        group = "rpc4k"
        useJUnitPlatform()
        filter {
            includeTest("TestServers", "userProtocol")
        }

        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = true
            showStackTraces = true
            showExceptions = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }

        fun portIsAvailable(port: Int): Boolean {
            return try {
                ServerSocket(port).close()
                true
            } catch (e: BindException) {
                false
            }
        }

        this.onlyIf { portIsAvailable(8080) }
    }

    tasks.create("setupClientTesting") {
        group = "rpc4k"
        dependsOn(startUserProtocolServer)
    }
}



