
plugins {
    alias(libs.plugins.ksp)
    id("io.github.natanfudge.rpc4k")
}

rpc4k {
    dev = false
    typescriptDir = rootDir.parentFile.resolve("typescript/testprodapp/src/generated")
}

version = "1.0-SNAPSHOT"

tasks.test {
    useJUnitPlatform()
}


dependencies {
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))
    testImplementation(Testing.Strikt.core)
    testImplementation(libs.okhttp.core)
    testImplementation(libs.ktor.netty)
    testImplementation(libs.logback)
}
