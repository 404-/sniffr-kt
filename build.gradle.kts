import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.20"
}

group = "fun.notfound"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.vavr:vavr:0.10.4")
    implementation("io.arrow-kt:arrow-core:1.1.5")

    testImplementation(kotlin("test"))
    testImplementation("com.google.code.gson:gson:2.10.1")
    testImplementation("io.kotest:kotest-runner-junit5:5.5.5")
}

tasks.test {
    useJUnitPlatform()
}

val jv = JavaVersion.VERSION_17

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = jv.majorVersion
}

kotlin {
    jvmToolchain(jv.majorVersion.toInt())

}
