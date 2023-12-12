
plugins {
   alias(libs.plugins.kotlin)
    alias(libs.plugins.serialization)
    id("import com.caesarealabs.natanfudge.rpc4k") version "0.0.3"
}

rpc4k {
    typescriptDir = rootDir.parentFile.resolve("typescript/src/generated")
}

version = "1.0-SNAPSHOT"

tasks.test {
    useJUnitPlatform()
}
repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))
    testImplementation(libs.strikt)
    testImplementation(libs.okhttp.core)
    testImplementation(libs.ktor.netty)
    testImplementation(libs.logback)
}
