import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.metro)
}

val quizRuntime = rootProject.extra["quizRuntime"] as String
check(quizRuntime in setOf("fake", "prod")) {
    "quiz.runtime must be 'fake' or 'prod' (was '$quizRuntime')."
}
val quizRuntimeSourceSetDir = if (quizRuntime == "prod") "prodMain" else "fakeMain"

kotlin {
    android {
        namespace = "com.droidkaigi.quiz.library"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    jvm {
        compilerOptions {
            jvmTarget.set(
                if (quizRuntime == "prod") JvmTarget.JVM_17 else JvmTarget.JVM_11,
            )
        }
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain {
            kotlin.srcDir("src/$quizRuntimeSourceSetDir/kotlin")
        }
        commonMain.dependencies {
            implementation(project(":core:domain"))
            implementation(project(":core:data"))
            implementation(project(":core:ui"))
            implementation(project(":feature:quiz"))
            implementation(project(":feature:ranking"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.ui)
            implementation(libs.compose.ui.util)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.navigation3.runtime)
            implementation(libs.jetbrains.navigation3.ui)
            implementation(libs.jetbrains.lifecycle.viewmodel.navigation3)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.metro.runtime)
        }
    }
}

configurations.configureEach {
    if (name.contains("jvm", ignoreCase = true)) {
        exclude(group = "androidx.compose.ui", module = "ui-util-jvmstubs")
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.ui.tooling)
}
