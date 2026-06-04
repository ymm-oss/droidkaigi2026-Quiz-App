/**
 * Resolves quiz.runtime for KMP modules (single Android library variant).
 *
 * Priority: Android flavor in task name (assembleProdDebug, …) > -Pquiz.runtime / gradle.properties > fake
 *
 * gradle.properties の既定 fake より Android Build Variant を優先する（prodDebug APK に fake が入るのを防ぐ）。
 */
val resolvedQuizRuntime: String = run {
    val flavorInTask =
        Regex(
            """(?:^|:)(?:assemble|install|bundle|connected)(Fake|Prod)(?:Debug|Release)""",
            RegexOption.IGNORE_CASE,
        )
    val detected = gradle.startParameter.taskNames.mapNotNull { task ->
        flavorInTask.find(task)?.groupValues?.get(1)?.lowercase()
    }.toSet()

    val fromVariant = when (detected.size) {
        1 -> detected.single()
        else -> {
            if (detected.size > 1) {
                logger.lifecycle(
                    "quiz.runtime: multiple flavors in one invocation ($detected); " +
                        "KMP uses a single runtime. Defaulting to fake. " +
                        "Build one variant per Gradle call or pass -Pquiz.runtime=prod.",
                )
            }
            null
        }
    }
    if (fromVariant != null) return@run fromVariant

    val explicit = providers.gradleProperty("quiz.runtime").orNull
    if (explicit == "prod" || explicit == "fake") return@run explicit

    "fake"
}

rootProject.extra["quizRuntime"] = resolvedQuizRuntime
logger.lifecycle("quiz.runtime resolved to '$resolvedQuizRuntime' (tasks=${gradle.startParameter.taskNames})")
