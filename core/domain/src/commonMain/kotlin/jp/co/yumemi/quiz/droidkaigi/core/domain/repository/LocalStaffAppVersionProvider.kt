package jp.co.yumemi.quiz.droidkaigi.core.domain.repository

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.LocalStaffAppVersion

interface LocalStaffAppVersionProvider {
    fun current(): LocalStaffAppVersion

    /** Desktop DMG auto-update. False on Android / Wasm (no local staff Desktop binary). */
    val supportsDesktopAutoUpdate: Boolean
        get() = true
}
