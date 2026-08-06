import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

val quizRuntime = rootProject.extra["quizRuntime"] as String
check(quizRuntime in setOf("fake", "prod")) {
    "quiz.runtime must be 'fake' or 'prod' (was '$quizRuntime')."
}
apply(from = "${rootDir}/gradle/desktop-jlink-modules.gradle.kts")
val desktopJlinkModules = rootProject.extra["desktopJlinkModules"] as List<String>
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
            // See gradle/desktop-jlink-modules.gradle.kts
            modules(*desktopJlinkModules.toTypedArray())
        }
    }
}

apply(from = "${rootDir}/gradle/firebase-google-services.desktop.gradle.kts")
