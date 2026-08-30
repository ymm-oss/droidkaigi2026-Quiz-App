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

val quizRuntime = rootProject.extra["quizRuntime"] as String
check(quizRuntime in setOf("fake", "prod")) {
    "quiz.runtime must be 'fake' or 'prod' (was '$quizRuntime')."
}
val quizRuntimeSourceSetDir = if (quizRuntime == "prod") "prodMain" else "fakeMain"

/**
 * wasm(prod) 用: google-services.json から Firebase JS SDK の初期化定数を生成する。
 * JVM（GoogleServicesLoader）と同じ設定ファイルを単一ソースとして共有する。
 * 明示指定する場合は `-Pquiz.firebase.config=/path/to/google-services.json`。
 */
val generateFirebaseWebConfig = tasks.register("generateFirebaseWebConfig") {
    val configFile = providers.gradleProperty("quiz.firebase.config").orNull
        ?.let { rootProject.file(it) }
        ?: rootProject.file("androidApp/src/prod/google-services.json")
    val outputDir = layout.buildDirectory.dir("generated/firebaseWebConfig/kotlin")
    inputs.property("configPath", configFile.absolutePath)
    if (configFile.isFile) {
        inputs.file(configFile)
    }
    outputs.dir(outputDir)
    doLast {
        check(configFile.isFile) {
            "google-services.json が見つかりません (${configFile.absolutePath})。" +
                " androidApp/src/prod/google-services.json を配置するか、" +
                " -Pquiz.firebase.config=/絶対パス/google-services.json を指定してください。"
        }

        @Suppress("UNCHECKED_CAST")
        val root = groovy.json.JsonSlurper().parse(configFile) as Map<String, Any?>
        val projectInfo = root["project_info"] as? Map<String, Any?>
            ?: error("google-services.json に project_info が見つかりません")
        val projectId = projectInfo["project_id"] as? String
            ?: error("google-services.json に project_id が見つかりません")
        val client = (root["client"] as? List<Map<String, Any?>>)?.firstOrNull()
            ?: error("google-services.json に client が見つかりません")
        val apiKey = (client["api_key"] as? List<Map<String, Any?>>)
            ?.firstOrNull()?.get("current_key") as? String
            ?: error("google-services.json に api_key が見つかりません")
        val applicationId = (client["client_info"] as? Map<String, Any?>)
            ?.get("mobilesdk_app_id") as? String
            ?: error("google-services.json に mobilesdk_app_id が見つかりません")

        fun escape(value: String) = value.replace("\\", "\\\\").replace("\"", "\\\"")
        val outputFile = outputDir.get()
            .file("jp/co/yumemi/quiz/droidkaigi/core/data/firestore/FirebaseWebConfig.kt").asFile
        outputFile.parentFile.mkdirs()
        outputFile.writeText(
            """
            |// generateFirebaseWebConfig タスクが google-services.json から生成する。手動編集しない。
            |package jp.co.yumemi.quiz.droidkaigi.core.data.firestore
            |
            |internal object FirebaseWebConfig {
            |    const val PROJECT_ID = "${escape(projectId)}"
            |    const val API_KEY = "${escape(apiKey)}"
            |    const val APPLICATION_ID = "${escape(applicationId)}"
            |    const val AUTH_DOMAIN = "${escape(projectId)}.firebaseapp.com"
            |}
            |""".trimMargin(),
        )
    }
}

kotlin {
    android {
        namespace = "jp.co.yumemi.quiz.droidkaigi.core.data"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        androidResources {
            enable = true
        }
        withHostTest {}
    }
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
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
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.compose.runtime)
            implementation(libs.compose.components.resources)
            implementation(libs.metro.runtime)
        }
        // fake / prod で yarn.lock が揺れないよう常時宣言する。
        // fake では @JsModule の import が存在しないため webpack バンドルには含まれない。
        wasmJsMain.dependencies {
            implementation(npm("firebase", libs.versions.firebase.js.sdk.get()))
            implementation(npm("@js-joda/core", libs.versions.js.joda.core.get()))
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
    if (quizRuntime == "prod") {
        val prodGitLiveDir = "src/prodGitLive/kotlin"
        sourceSets.named("androidMain").configure {
            kotlin.srcDir("src/prodAndroid/kotlin")
            kotlin.srcDir(prodGitLiveDir)
            dependencies {
                implementation(libs.firebase.common.lib)
                implementation(libs.firebase.auth.ktx)
                implementation(libs.firebase.firestore.ktx)
                implementation(libs.firebase.app)
                implementation(libs.firebase.auth)
                implementation(libs.firebase.firestore)
            }
        }
        sourceSets.named("jvmMain").configure {
            kotlin.srcDir("src/prodJvm/kotlin")
            kotlin.srcDir(prodGitLiveDir)
            dependencies {
                implementation(libs.firebase.app)
                implementation(libs.firebase.auth)
                implementation(libs.firebase.firestore)
                implementation(libs.firebase.java.sdk)
                // GitLive on JVM bridges to Android Firebase, which posts Auth listeners to Main.
                implementation(libs.kotlinx.coroutines.swing)
            }
        }
        sourceSets.named("wasmJsMain").configure {
            kotlin.srcDir("src/prodWasm/kotlin")
            kotlin.srcDir(generateFirebaseWebConfig)
        }
    }
    if (quizRuntime != "fake") {
        sourceSets.named("jvmTest").configure {
            kotlin.exclude(
                "**/FakeRankingRepositoryTest.kt",
                "**/InMemoryQuizCatalogSitePublishedTest.kt",
            )
        }
    } else {
        // prod ソース（prodMain）に依存するテストは fake ではコンパイル対象外
        sourceSets.named("jvmTest").configure {
            kotlin.exclude("**/BaseFirestoreServiceTest.kt")
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "jp.co.yumemi.quiz.droidkaigi.core.data.generated.resources"
}

if (quizRuntime == "prod") {
    apply(from = "${rootDir}/gradle/firebase-google-services.desktop.gradle.kts")
}
