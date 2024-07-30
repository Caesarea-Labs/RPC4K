pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

plugins {
    id("de.fayard.refreshVersions") version "0.60.5"
}

rootProject.name = "RPC4K"
include(":testapp")
// Multiplatform, exposed to runtime users of RPC4K
include(":lib")
// JVM Targeted, runs during kotlin Compilation
include(":processor")
// JVM Targeted, runs as part of the Gradle script to pull in RPC libraries and such
includeBuild("plugin")

val linkLogging = true
val loggingDir = file("../../Loggy/logging")
if (linkLogging) {
    includeBuild(loggingDir) {
        dependencySubstitution {
            substitute(module("com.caesarealabs:logging")).using(project(":"))
        }
    }
}