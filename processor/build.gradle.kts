
group = "com.example"
version = "1.0-SNAPSHOT"


dependencies {
    implementation(libs.symbol.processing.api)
    implementation(libs.kotlinpoet)
    implementation(libs.okhttp.sse)
    implementation(libs.ktor)
    api(libs.coroutines.core)
    api(libs.serialization.json)

    testImplementation(libs.kotlin.test)
    testImplementation (libs.compile.testing.ksp)
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

