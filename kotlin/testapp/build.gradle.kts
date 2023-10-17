import java.net.BindException
import java.net.ServerSocket

plugins {
    alias(libs.plugins.ksp)
}

version = "1.0-SNAPSHOT"

tasks.test {
    useJUnitPlatform()
}


dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":lib"))
    "ksp"(project(":lib"))
    testImplementation(kotlin("test"))
    testImplementation(Testing.Strikt.core)
    testImplementation(libs.okhttp.core)
    testImplementation(libs.ktor.netty)
    testImplementation(libs.logback)
}


afterEvaluate {
    val copyApiDefinitionTasks = sourceSets.map { sourceSet ->
        val sourceSetTaskName = if (sourceSet.name == "main") "" else sourceSet.name.replaceFirstChar { it.uppercaseChar() }
        tasks.create("copy${sourceSetTaskName}ApiDefinitions", Copy::class) {
            group = "rpc4k"
            val kspTask = tasks.getByName("ksp${sourceSetTaskName}Kotlin")
            dependsOn(kspTask)
            val rpc4kResourceDir = project.layout.buildDirectory.dir("generated/ksp/${sourceSet.name}/resources/rpc4k")
            from(rpc4kResourceDir)
            into(rootProject.layout.projectDirectory.dir("../typescript/test/generated/definitions"))
        }
    }


    val copyAllApiDefinitions by tasks.creating {
        group = "rpc4k"
        dependsOn(copyApiDefinitionTasks)
    }

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
        dependsOn(copyAllApiDefinitions, startUserProtocolServer)
    }
}



