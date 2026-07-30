import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

val quizRuntime = rootProject.extra["quizRuntime"] as String
val appVersion = rootProject.extra["appVersion"] as String
val appVersionCode = rootProject.extra["appVersionCode"] as Int
val hasProdFirebaseConfig = file("src/prod/google-services.json").exists()
if (quizRuntime == "prod" && hasProdFirebaseConfig) {
    apply(plugin = libs.plugins.googleServices.get().pluginId)
    apply(plugin = libs.plugins.firebaseCrashlytics.get().pluginId)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

android {
    namespace = "com.droidkaigi.quiz"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    ndkVersion = libs.versions.android.ndk.get()

    defaultConfig {
        applicationId = "com.droidkaigi.quiz"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = appVersionCode
        versionName = appVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    flavorDimensions += "runtime"
    productFlavors {
        create("fake") {
            dimension = "runtime"
            isDefault = true
            applicationIdSuffix = ".fake"
            versionNameSuffix = "-fake"
        }
        create("prod") {
            dimension = "runtime"
            // applicationId は google-services.json の package_name（com.droidkaigi.quiz）と一致させる
            versionNameSuffix = "-prod"
        }
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            keepDebugSymbols += "**/libandroidx.graphics.path.so"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(projects.composeApp)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    add("prodImplementation", platform(libs.firebase.bom))
    add("prodImplementation", libs.firebase.crashlytics)

    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.testExt.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.kotlin.test.junit)
    // Screenshot tests render feature composables directly (no navigation driving).
    androidTestImplementation(projects.core.domain)
    androidTestImplementation(projects.core.ui)
    androidTestImplementation(projects.feature.quiz)
    androidTestImplementation(projects.feature.ranking)
}
