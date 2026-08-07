import org.gradle.api.tasks.JavaExec
import org.gradle.language.jvm.tasks.ProcessResources

/**
 * Desktop JVM apps read Firebase client config from a single canonical file:
 * androidApp/src/prod/google-services.json
 *
 * - `run`: system property droidkaigi.firebase.config
 * - packaged DMG/MSI/Deb: classpath google-services.json (via processResources / jvmProcessResources)
 */
val firebaseGoogleServicesFile =
    rootProject.layout.projectDirectory.file("androidApp/src/prod/google-services.json")

afterEvaluate {
    val quizRuntime = rootProject.extra["quizRuntime"] as String
    val shouldBundleFirebase = quizRuntime == "prod" && firebaseGoogleServicesFile.asFile.exists()

    fun ProcessResources.bundleFirebaseConfig() {
        if (shouldBundleFirebase) {
            from(firebaseGoogleServicesFile) {
                rename { "google-services.json" }
            }
        }
    }

    tasks.withType<ProcessResources>().configureEach {
        if (name == "processResources" || name == "jvmProcessResources") {
            bundleFirebaseConfig()
        }
    }

    tasks.withType<JavaExec>().configureEach {
        workingDir = rootProject.layout.projectDirectory.asFile
        if (firebaseGoogleServicesFile.asFile.isFile) {
            systemProperty(
                "droidkaigi.firebase.config",
                firebaseGoogleServicesFile.asFile.absolutePath,
            )
        }
    }
}
