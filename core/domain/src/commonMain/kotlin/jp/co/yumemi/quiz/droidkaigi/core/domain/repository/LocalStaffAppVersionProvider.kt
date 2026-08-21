package jp.co.yumemi.quiz.droidkaigi.core.domain.repository

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.LocalStaffAppVersion

interface LocalStaffAppVersionProvider {
    fun current(): LocalStaffAppVersion
}
