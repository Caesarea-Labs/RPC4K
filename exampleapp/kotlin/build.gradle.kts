plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ksp)
    id("com.caesarealabs.rpc4k") version "0.8.0"
}

//repositories {
//    mavenLocal()
//}

//rpc4k {
//    typescriptDir = rootDir.parentFile.resolve("typescript/src/generated")
//}

version = "1.0-SNAPSHOT"

tasks.test {
    useJUnitPlatform()
}
repositories {
    mavenCentral()
}

dependencies {
//    ksp("com.caesarealabs:rpc4k-processor:0.8.0")
//    implementation("com.caesarealabs:rpc4k-runtime:0.8.0")
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))
    testImplementation(libs.strikt)
    testImplementation(libs.okhttp.core)
    testImplementation(libs.ktor.netty)
    testImplementation(libs.logback)
}
