import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.metro)
}

val quizRuntime = providers.gradleProperty("quiz.runtime").orElse("fake").get()
check(quizRuntime in setOf("fake", "prod")) {
    "quiz.runtime must be 'fake' or 'prod' (was '$quizRuntime'). Set it in gradle.properties."
}
val quizRuntimeSourceSetDir = if (quizRuntime == "prod") "prodMain" else "fakeMain"

kotlin {
    android {
        namespace = "com.droidkaigi.quiz.core.data"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
        androidResources {
            enable = true
        }
        withHostTest {}
    }
    jvm()
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
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.compose.runtime)
            implementation(libs.compose.components.resources)
            implementation(libs.metro.runtime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
    if (quizRuntime != "fake") {
        sourceSets.named("jvmTest").configure {
            kotlin.exclude("**/FakeRankingRepositoryTest.kt")
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.droidkaigi.quiz.core.data.generated.resources"
}
