/**
 * Resolves app.version / app.versionCode for Android and Desktop packaging.
 *
 * Override with -Papp.version=1.2.0 and optionally -Papp.versionCode=10200.
 * When versionCode is omitted, it is derived from SemVer: major*10000 + minor*100 + patch.
 */
val appVersion: String = providers.gradleProperty("app.version").orElse("1.0.0").get()

val appVersionCode: Int = providers.gradleProperty("app.versionCode")
    .map { it.toInt() }
    .orElse(
        providers.provider {
            val match = Regex("""^(\d+)\.(\d+)\.(\d+)$""").matchEntire(appVersion)
                ?: error(
                    "app.version must be SemVer MAJOR.MINOR.PATCH (was '$appVersion'). " +
                        "Pass -Papp.version=1.2.0 or -Papp.versionCode=…",
                )
            val (major, minor, patch) = match.destructured
            major.toInt() * 10_000 + minor.toInt() * 100 + patch.toInt()
        },
    )
    .get()

rootProject.extra["appVersion"] = appVersion
rootProject.extra["appVersionCode"] = appVersionCode
logger.lifecycle("app.version=$appVersion app.versionCode=$appVersionCode")
