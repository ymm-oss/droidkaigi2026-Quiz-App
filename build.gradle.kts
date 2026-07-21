plugins {
    alias(libs.plugins.googleServices) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidKmpLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.metro) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.droidkaigiDetekt) apply false
}

apply(from = "gradle/quiz-runtime.gradle.kts")
apply(from = "gradle/version.gradle.kts")


subprojects {
    pluginManager.apply(rootProject.libs.plugins.droidkaigiDetekt.get().pluginId)
}

tasks.register("detektAll") {
    group = "verification"
    description = "Runs detekt on all subprojects"
}

tasks.register("detektFormat") {
    group = "verification"
    description = "Runs detekt with auto-correct on all subprojects"
}

subprojects {
    afterEvaluate {
        tasks.findByName("detektAll")?.let { detektAllTask ->
            rootProject.tasks.named("detektAll").configure {
                dependsOn(detektAllTask)
            }
        }
        tasks.findByName("detektFormat")?.let { detektFormatTask ->
            rootProject.tasks.named("detektFormat").configure {
                dependsOn(detektFormatTask)
            }
        }
    }
}
