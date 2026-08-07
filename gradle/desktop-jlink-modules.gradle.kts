/**
 * JDK modules required for prod Desktop when GitLive Firebase (Android SDK bridge) + Firestore run on JVM.
 * jlink does not bundle these unless listed in nativeDistributions { modules(...) }.
 *
 * Verify:
 *   ./gradlew :staffDesktopApp:suggestRuntimeModules -Pquiz.runtime=prod
 *   ./gradlew :staffDesktopApp:runDistributable -Pquiz.runtime=prod
 *   cat staffDesktopApp/build/compose/tmp/main/runtime/release
 */
rootProject.extra["desktopJlinkModules"] = listOf(
    "java.sql",
    "jdk.unsupported",
    "java.instrument",
)
