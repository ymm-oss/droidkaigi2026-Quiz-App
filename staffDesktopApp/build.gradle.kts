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
    val outputDir = layout.buildDirectory.dir("generated/staffAppVersion")
    val outputFile = outputDir.map { it.file("staff-app-version.properties") }
    inputs.property("appVersion", appVersion)
    inputs.property("appVersionCode", appVersionCode)
    outputs.file(outputFile)
    doLast {
        val file = outputFile.get().asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            version=$appVersion
            versionCode=$appVersionCode
            """.trimIndent() + "\n",
        )
    }
}

tasks.named<ProcessResources>("processResources") {
    dependsOn(generateStaffAppVersionProperties)
    from(layout.buildDirectory.dir("generated/staffAppVersion"))
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
