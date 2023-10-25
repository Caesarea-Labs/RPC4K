dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../../kotlin/gradle/libs.versions.toml"))
        }
    }
}