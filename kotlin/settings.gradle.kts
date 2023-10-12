
pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}
plugins {
    id("de.fayard.refreshVersions") version "0.60.3"
}

rootProject.name = "RPC4K"
include(":testapp")
include(":lib")
