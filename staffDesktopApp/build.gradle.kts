import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

val quizRuntime = rootProject.extra["quizRuntime"] as String
val packageAppVersion = rootProject.extra["appVersion"] as String
val packageAppVersionCode = rootProject.extra["appVersionCode"] as Int
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
        mainClass = "jp.co.yumemi.quiz.droidkaigi.staff.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "jp.co.yumemi.quiz.droidkaigi.staff"
            packageVersion = packageAppVersion
            // See gradle/desktop-jlink-modules.gradle.kts
            modules(*desktopJlinkModules.toTypedArray())
        }
    }
}

abstract class GenerateStaffAppVersionPropertiesTask : DefaultTask() {
    @get:Input
    abstract val appVersion: Property<String>

    @get:Input
    abstract val appVersionCode: Property<Int>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val file = outputFile.get().asFile
        file.parentFile.mkdirs()
        file.writeText("version=${appVersion.get()}\nversionCode=${appVersionCode.get()}\n")
    }
}

val generateStaffAppVersionProperties =
    tasks.register<GenerateStaffAppVersionPropertiesTask>("generateStaffAppVersionProperties") {
        appVersion.set(packageAppVersion)
        appVersionCode.set(packageAppVersionCode)
        outputFile.set(layout.buildDirectory.file("generated/staffAppVersion/staff-app-version.properties"))
    }

tasks.named<ProcessResources>("processResources") {
    from(generateStaffAppVersionProperties)
}

apply(from = "${rootDir}/gradle/firebase-google-services.desktop.gradle.kts")
