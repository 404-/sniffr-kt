plugins {
    id("banal")
}

version = "0.1.0"

dependencies {
    implementation(project(":api"))
    implementation(project(":utils"))
    implementation(project(":impl:common"))

    implementation("io.vavr:vavr:0.10.4")
    implementation("io.arrow-kt:arrow-core:1.2.4")
    implementation("io.arrow-kt:arrow-eval:1.2.4")

    testImplementation(kotlin("test"))
    testImplementation(project(":testkit"))
    testImplementation(project(":langs:gson"))
    testImplementation("com.google.code.gson:gson:2.10.1")
    testImplementation("io.kotest:kotest-runner-junit5:5.9.0")
}
