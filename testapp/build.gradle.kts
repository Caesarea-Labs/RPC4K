plugins {
    alias(libs.plugins.ksp)
}

version = "1.0-SNAPSHOT"

tasks.test {
    useJUnitPlatform()
}


dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":processor"))
    "ksp"(project(":processor"))
    testImplementation(kotlin("test"))
    testImplementation(Testing.Strikt.core)
    testImplementation(libs.okhttp.core)
    testImplementation(libs.ktor)
    testImplementation(libs.logback)
}

sourceSets {
    create("errors") {
        compileClasspath += test.get().compileClasspath
    }
}
