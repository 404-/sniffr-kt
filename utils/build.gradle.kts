plugins {
    id("banal")
}

version = "0.1.0"

dependencies {
    implementation(libs.vavr)
    implementation(libs.arrow.core)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotest.runner)
}
