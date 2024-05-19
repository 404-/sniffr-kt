plugins {
    id("banal")
}

version = "0.1.0"

dependencies {
    implementation(project(":api"))

    implementation(libs.vavr)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotest.runner)
}
