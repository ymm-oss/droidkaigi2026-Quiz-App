import org.gradle.api.tasks.JavaExec
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

val quizRuntime = providers.gradleProperty("quiz.runtime").orElse("fake").get()
check(quizRuntime in setOf("fake", "prod")) {
    "quiz.runtime must be 'fake' or 'prod' (was '$quizRuntime')."
}
val prodJvm = quizRuntime == "prod"

kotlin {
    jvmToolchain(if (prodJvm) 17 else 11)
    compilerOptions {
        jvmTarget.set(if (prodJvm) JvmTarget.JVM_17 else JvmTarget.JVM_11)
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

val rootFirebaseConfig = rootProject.layout.projectDirectory.file("androidApp/google-services.json")
tasks.withType<JavaExec>().configureEach {
    if (name == "run") {
        workingDir = rootProject.layout.projectDirectory.asFile
        if (rootFirebaseConfig.asFile.isFile) {
            systemProperty("droidkaigi.firebase.config", rootFirebaseConfig.asFile.absolutePath)
        }
    }
}
