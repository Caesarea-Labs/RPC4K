plugins {
    id("com.google.devtools.ksp")
    kotlin("jvm")
    kotlin("plugin.serialization")
}

version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}
val kotlin_serialization_runtime_version: String by project
dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":test-processor"))
//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlin_serialization_runtime_version")
    ksp(project(":test-processor"))
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13")

}

ksp {
    arg("option1", "value1")
    arg("option2", "value2")
}
