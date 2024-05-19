rootProject.name = "sniffr-kt"

include(
    "api",
    "impl:eval", "impl:drf",
    "langs:gson",
    "impl:common", "utils", "testkit"
)

// https://docs.gradle.org/current/userguide/platforms.html
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val v = object {
                val vavr = "0.10.4"
                val gson = "2.10.1"
                val arrow = "1.2.4"
                val kotest = "5.9.0"
            }

            version("vavr", v.vavr)
            library("vavr", "io.vavr", "vavr").version(v.vavr)

            version("gson", v.gson)
            library("gson", "com.google.code.gson", "gson").version(v.gson)

            version("arrow", v.arrow)
            listOf("arrow-core", "arrow-eval", "arrow-fx-coroutines").forEach { artifact ->
                library(/*alias*/ artifact, "io.arrow-kt", artifact).version(v.arrow)
            }

            version("kotest", v.kotest)
            library("kotest-runner", "io.kotest", "kotest-runner-junit5").version(v.kotest)
        }
    }
}
