pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}
plugins {
    id("de.fayard.refreshVersions") version "0.60.5"
}

rootProject.name = "RPC4K"
include(":testapp")
include(":lib")
includeBuild("plugin")