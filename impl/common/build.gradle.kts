import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.24"
}

group = "fun.notfound"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":api"))

    implementation("io.vavr:vavr:0.10.4")

    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-runner-junit5:5.9.0")
}

tasks.test {
    useJUnitPlatform()
}

val jv = JavaVersion.VERSION_21

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = jv.majorVersion
    }
}

kotlin {
    jvmToolchain(jv.majorVersion.toInt())
}