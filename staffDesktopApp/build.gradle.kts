import org.gradle.api.tasks.JavaExec
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

val quizRuntime = rootProject.extra["quizRuntime"] as String
val appVersion = rootProject.extra["appVersion"] as String
val appVersionCode = rootProject.extra["appVersionCode"] as Int
check(quizRuntime in setOf("fake", "prod")) {
    "quiz.runtime must be 'fake' or 'prod' (was '$quizRuntime')."
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
            packageVersion = appVersion
        }
    }
}

val generateStaffAppVersionProperties = tasks.register("generateStaffAppVersionProperties") {
    val version = appVersion
    val versionCode = appVersionCode
    val outputFile = layout.buildDirectory.file("generated/staffAppVersion/staff-app-version.properties")
    inputs.property("appVersion", version)
    inputs.property("appVersionCode", versionCode)
    outputs.file(outputFile)
    doLast {
        val file = outputFile.get().asFile
        file.parentFile.mkdirs()
        file.writeText("version=$version\nversionCode=$versionCode\n")
    }
}

tasks.named<ProcessResources>("processResources") {
    from(generateStaffAppVersionProperties)
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
