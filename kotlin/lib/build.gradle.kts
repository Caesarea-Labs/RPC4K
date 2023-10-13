

group = "com.example"
version = "1.0-SNAPSHOT"


dependencies {
    implementation(libs.symbol.processing.api)
    implementation(libs.kotlinpoet.core)
    implementation(libs.kotlinpoet.ksp)

    implementation("com.michael-bull.kotlin-result:kotlin-result:1.1.18")
    api(libs.coroutines.core)
    api(libs.serialization.json)
//    implementation("dev.adamko.kxstsgen:kxs-ts-gen-core:0.2.1")

    //TODO: these should be separate modules
    /////////////////////////////////////////
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.sse)
    implementation(libs.ktor.host)
    implementation("io.ktor:ktor-server-call-logging-jvm:2.2.4")
////////////////////////////////////////////

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

