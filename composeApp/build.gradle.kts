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

val quizRuntime = providers.gradleProperty("quiz.runtime").orElse("fake").get()
check(quizRuntime in setOf("fake", "prod")) {
    "quiz.runtime must be 'fake' or 'prod' (was '$quizRuntime'). Set it in gradle.properties."
}

kotlin {
    android {
        namespace = "com.droidkaigi.quiz.library"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    jvm()
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        val fakeMain by creating {
            dependsOn(commonMain.get())
        }
        val prodMain by creating {
            dependsOn(commonMain.get())
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
        fakeMain.dependencies {
            implementation(project(":core:data"))
        }
        prodMain.dependencies {
            implementation(project(":core:data"))
        }
    }

    val activeRuntimeMain = sourceSets.getByName(if (quizRuntime == "prod") "prodMain" else "fakeMain")
    val common = sourceSets.getByName("commonMain")
    sourceSets.named("jvmMain").configure {
        dependsOn(common)
        dependsOn(activeRuntimeMain)
    }
    sourceSets.named("androidMain").configure {
        dependsOn(common)
        dependsOn(activeRuntimeMain)
    }
    sourceSets.named("wasmJsMain").configure {
        dependsOn(common)
        dependsOn(activeRuntimeMain)
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
