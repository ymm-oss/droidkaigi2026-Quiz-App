import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.metro)
}

val quizRuntime = rootProject.extra["quizRuntime"] as String
val usesProdBackend = rootProject.extra["usesProdBackend"] as Boolean
check(quizRuntime in setOf("fake", "prod", "local")) {
    "quiz.runtime must be 'fake', 'prod', or 'local' (was '$quizRuntime')."
}
val quizRuntimeSourceSetDir = if (usesProdBackend) "prodMain" else "fakeMain"

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {
        commonMain {
            kotlin.srcDir("src/$quizRuntimeSourceSetDir/kotlin")
        }
        commonMain.dependencies {
            implementation(project(":core:data"))
            implementation(project(":core:ui"))
            implementation(project(":feature:staff"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.metro.runtime)
        }
    }
}

configurations.configureEach {
    if (name.contains("jvm", ignoreCase = true)) {
        exclude(group = "androidx.compose.ui", module = "ui-util-jvmstubs")
    }
}
