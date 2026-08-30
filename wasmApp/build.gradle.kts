import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "droidkaigiQuiz.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        wasmJsMain.dependencies {
            implementation(projects.composeApp)
            implementation(projects.staffComposeApp)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            // Executable webpack がライブラリの npm を解決できないことがあるため、
            // バンドル入口でも宣言する（core:data の firebase、kotlinx-datetime の js-joda）。
            implementation(npm("firebase", libs.versions.firebase.js.sdk.get()))
            implementation(npm("@js-joda/core", libs.versions.js.joda.core.get()))
        }
    }
}
