val kspVersion: String by project

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}
val kotlin_serialization_runtime_version: String by project
dependencies {
    testImplementation(kotlin("test"))
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
    implementation("com.squareup:kotlinpoet:1.9.0")
    testImplementation ("com.github.tschuchortdev:kotlin-compile-testing:1.4.2")
    testImplementation("junit:junit:4.13.2")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
//    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.2.0")
    testImplementation ("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.4.2")
}



tasks.test {
    useJUnit()
    testLogging {
        events("passed", "skipped", "failed", "standardOut", "standardError")
    }
}

sourceSets.main {
    java.srcDirs("src/main/kotlin")
}

