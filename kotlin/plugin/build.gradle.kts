plugins {
    `kotlin-dsl`
    embeddedKotlin("plugin.serialization")
    id("com.gradle.plugin-publish") version "1.1.0"
}

group = "com.caesarealabs"
version = libs.versions.rpc4k.get()

repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
}

dependencies {
    implementation(libs.nodejs)
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:${libs.versions.ksp.get()}")
}


gradlePlugin {
    website = "https://github.com/natanfudge/rpc4k"
    vcsUrl = "https://github.com/natanfudge/rpc4k"
    plugins {
        create("rpc4kPlugin") {
            id = "com.caesarealabs.rpc4k"
            implementationClass = "com.caesarealabs.rpc4k.gradle.Rpc4KPlugin"
            tags = listOf("rpc", "annotation processor", "ksp", "rpc4all")
            displayName = "Rpc4k"
            description = "Sets up rpc4k - a framework for interfacing between services in different programming languages. To get started view the @Api annotation docs."
        }
    }
}


kotlin {
    compilerOptions.freeCompilerArgs.add("-Xcontext-receivers")
}

fun rpc4kRuntimeVersion() = libs.versions.rpc4k.get()


tasks.withType<ProcessResources> {
    val versionProp = "version" to rpc4kRuntimeVersion()
    inputs.property("version", rpc4kRuntimeVersion())
    from(sourceSets.main.get().resources.srcDirs) {
        include("rpc4k_version.txt")
        expand(versionProp)
    }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}