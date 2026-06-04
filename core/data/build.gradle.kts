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
        val fakeMain by creating {
            dependsOn(commonMain.get())
        }
        val prodMain by creating {
            dependsOn(commonMain.get())
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
        fakeMain.dependencies {
            implementation(libs.compose.components.resources)
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
    sourceSets.named("jvmTest").configure {
        dependsOn(sourceSets.named("jvmMain").get())
        if (quizRuntime != "fake") {
            kotlin.exclude("**/FakeRankingRepositoryTest.kt")
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.droidkaigi.quiz.core.data.generated.resources"
}
