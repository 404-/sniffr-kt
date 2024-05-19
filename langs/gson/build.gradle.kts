plugins {
    id("banal")
}

version = "0.1.0"

dependencies {
    implementation(project(":api"))

    implementation("io.arrow-kt:arrow-core:1.2.4")
    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-runner-junit5:5.9.0")
}
