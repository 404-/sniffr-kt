import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // the desired target version of kotlin cannot be specified explicitly in the plugin;
    // it's specified by [TargetKotlinVersion] in buildSrc/build.gradle.kts
    //   https://docs.gradle.org/current/userguide/implementing_gradle_plugins_precompiled.html#sec:applying_external_plugins
    kotlin("jvm")
}

group = "fun.notfound"

repositories {
    mavenCentral()
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
