plugins {
    alias(libs.plugins.ksp)
}

group = "com.example"
version = "1.0-SNAPSHOT"


dependencies {
    implementation(libs.symbol.processing.api)
    implementation(libs.kotlinpoet.core)
    implementation(libs.kotlinpoet.ksp)
    compileOnly(libs.okhttp.core)
    compileOnly(libs.okhttp.sse)
    compileOnly(libs.ktor)
    implementation("com.michael-bull.kotlin-result:kotlin-result:1.1.18")
    api(libs.coroutines.core)
    api(libs.serialization.json)
    implementation("com.squareup:kotlinpoet-ksp:<version>")


    testImplementation(libs.kotlin.test)
    testImplementation (libs.compile.testing.ksp)
    testImplementation(Testing.Strikt.core)
    testImplementation("ch.qos.logback:logback-classic:1.4.11")
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

