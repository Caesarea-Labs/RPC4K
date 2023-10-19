

group = "com.example"
version = "1.0-SNAPSHOT"


dependencies {
    implementation(libs.symbol.processing.api)
    implementation(libs.kotlinpoet.core)
    implementation(libs.kotlinpoet.ksp)

    api(libs.coroutines.core)
    api(libs.serialization.json)

    implementation(libs.okhttp.core)
    implementation(libs.okhttp.sse)
    implementation(libs.ktor.host)
    implementation(libs.ktor.logging)

    testImplementation(libs.kotlin.test)
    testImplementation (libs.compile.testing.ksp)
    testImplementation(Testing.Strikt.core)
    testImplementation(libs.logback)

}



tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed", "standardOut", "standardError")
    }
}

sourceSets.main {
    java.srcDirs("src/main/kotlin")
}

