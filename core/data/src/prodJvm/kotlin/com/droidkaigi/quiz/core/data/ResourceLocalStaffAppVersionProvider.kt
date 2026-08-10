package com.droidkaigi.quiz.core.data

import com.droidkaigi.quiz.core.data.di.AppScope
import com.droidkaigi.quiz.core.domain.model.LocalStaffAppVersion
import com.droidkaigi.quiz.core.domain.repository.LocalStaffAppVersionProvider
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.util.Properties

/**
 * Reads packaged `/staff-app-version.properties` written by `:staffDesktopApp` packaging.
 * Falls back to 0.0.0 / 0 when missing (treated as older than any published release).
 */
@Inject
@ContributesBinding(AppScope::class)
class ResourceLocalStaffAppVersionProvider : LocalStaffAppVersionProvider {
    override fun current(): LocalStaffAppVersion {
        val props = Properties()
        val stream = ResourceLocalStaffAppVersionProvider::class.java
            .getResourceAsStream("/staff-app-version.properties")
        if (stream != null) {
            stream.use { props.load(it) }
        }
        val version = props.getProperty("version")?.trim().orEmpty().ifBlank { "0.0.0" }
        val versionCode = props.getProperty("versionCode")?.trim()?.toIntOrNull() ?: 0
        return LocalStaffAppVersion(version = version, versionCode = versionCode)
    }
}
