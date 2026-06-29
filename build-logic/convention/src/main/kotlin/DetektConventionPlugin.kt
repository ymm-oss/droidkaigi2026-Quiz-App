import dev.detekt.gradle.Detekt
import dev.detekt.gradle.DetektCreateBaselineTask
import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("dev.detekt")

            val libs = rootProject.extensions.getByType<VersionCatalogsExtension>().named("libs")

            extensions.configure<DetektExtension> {
                buildUponDefaultConfig.set(true)
                parallel.set(true)
                config.from(rootProject.file("config/detekt/detekt.yml"))
                basePath.set(rootProject.projectDir)
                ignoredBuildTypes.set(listOf("release"))
            }

            dependencies {
                add("detektPlugins", libs.findLibrary("detekt-formatting").get())
            }

            val autoCorrectToggle = objects.property(Boolean::class.java).convention(false)

            tasks.withType<Detekt>().configureEach {
                notCompatibleWithConfigurationCache("detekt 2.0 alpha is not configuration-cache safe")
                exclude("**/build/**")
                exclude("**/generated/**")
                autoCorrect.set(autoCorrectToggle)
            }

            tasks.withType<DetektCreateBaselineTask>().configureEach {
                notCompatibleWithConfigurationCache("detekt 2.0 alpha is not configuration-cache safe")
                exclude("**/build/**")
                exclude("**/generated/**")
            }

            tasks.named("detekt", Detekt::class.java).configure {
                setSource(emptyList<Any>())
                dependsOn(
                    tasks.withType<Detekt>().matching { it.name != "detekt" },
                )
            }

            tasks.register("detektAll") {
                group = "verification"
                description = "Runs all detekt analysis tasks for this module"
                dependsOn(
                    tasks.withType<Detekt>().matching { !it.name.contains("Baseline", ignoreCase = true) },
                )
            }

            tasks.register("detektFormat") {
                group = "verification"
                description = "Runs detekt with auto-correct enabled for this module"
                autoCorrectToggle.set(true)
                dependsOn(
                    tasks.withType<Detekt>().matching { !it.name.contains("Baseline", ignoreCase = true) },
                )
            }
        }
    }
}
