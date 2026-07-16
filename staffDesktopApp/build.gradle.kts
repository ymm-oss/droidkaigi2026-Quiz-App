import org.gradle.api.tasks.JavaExec
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

val quizRuntime = rootProject.extra["quizRuntime"] as String
check(quizRuntime in setOf("fake", "prod", "local")) {
    "quiz.runtime must be 'fake', 'prod', or 'local' (was '$quizRuntime')."
}
kotlin {
    jvmToolchain(17)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(projects.staffComposeApp)
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutines.swing)
}

compose.desktop {
    application {
        mainClass = "com.droidkaigi.quiz.staff.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.droidkaigi.quiz.staff"
            packageVersion = "1.0.0"
        }
    }
}

val rootFirebaseConfig =
    rootProject.layout.projectDirectory.file("androidApp/src/prod/google-services.json")
tasks.withType<JavaExec>().configureEach {
    if (name == "run") {
        workingDir = rootProject.layout.projectDirectory.asFile
        if (rootFirebaseConfig.asFile.isFile) {
            systemProperty("droidkaigi.firebase.config", rootFirebaseConfig.asFile.absolutePath)
        }
    }
}
