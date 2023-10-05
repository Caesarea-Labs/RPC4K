plugins {
    alias(libs.plugins.ksp)
}

version = "1.0-SNAPSHOT"


dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":processor"))
    "ksp"(project(":processor"))
    testImplementation(kotlin("test"))
    testImplementation(Testing.Strikt.core)
}
sourceSets {
    main {
        java {
            srcDir(project.file("build/generated/ksp/src/main/kotlin"))
        }
    }
    test {
        java {
            srcDir(project.file("build/generated/ksp/src/test/kotlin"))
        }
    }
}

