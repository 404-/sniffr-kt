plugins {
    id("banal")
}

version = "0.1.0"

dependencies {
    implementation(project(":api"))

    implementation(libs.vavr)
    implementation(libs.gson)
    implementation(libs.kotest.runner)
}
