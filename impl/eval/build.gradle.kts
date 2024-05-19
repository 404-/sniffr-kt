plugins {
    id("banal")
}

version = "0.1.0"

dependencies {
    implementation(project(":api"))
    implementation(project(":utils"))
    implementation(project(":impl:common"))

    implementation(libs.vavr)
    implementation(libs.arrow.core)
    implementation(libs.arrow.eval)

    testImplementation(kotlin("test"))
    testImplementation(project(":testkit"))
    testImplementation(project(":langs:gson"))
    testImplementation(libs.gson)
    testImplementation(libs.kotest.runner)
}
