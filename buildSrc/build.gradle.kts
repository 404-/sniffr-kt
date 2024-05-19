// https://docs.gradle.org/current/samples/sample_convention_plugins.html

val TargetKotlinVersion = "1.9.24"

plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation(
        "org.jetbrains.kotlin",
        "kotlin-gradle-plugin",
        TargetKotlinVersion
    )
}
