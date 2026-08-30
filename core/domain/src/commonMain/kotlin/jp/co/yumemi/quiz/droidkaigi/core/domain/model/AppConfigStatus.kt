package jp.co.yumemi.quiz.droidkaigi.core.domain.model

/** Live snapshot of `appConfig/default` (site intake + published folder). */
data class AppConfigStatus(
    val sitePublished: Boolean,
    val activeFolderId: String,
)
