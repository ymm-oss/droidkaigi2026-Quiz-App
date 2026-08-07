package com.droidkaigi.quiz.core.domain.repository

import com.droidkaigi.quiz.core.domain.model.LocalStaffAppVersion

interface LocalStaffAppVersionProvider {
    fun current(): LocalStaffAppVersion
}
