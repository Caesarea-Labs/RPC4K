dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../../kotlin/gradle/libs.versions.toml"))
        }
    }
}
pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}