plugins {
    id("banal")
}

version = "0.1.0"

dependencies {
    implementation(project(":api"))

    implementation("io.vavr:vavr:0.10.4")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("io.kotest:kotest-runner-junit5:5.9.0")
}
