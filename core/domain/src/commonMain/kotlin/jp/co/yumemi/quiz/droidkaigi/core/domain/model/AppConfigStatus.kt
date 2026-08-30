package jp.co.yumemi.quiz.droidkaigi.core.domain.model

/** Live snapshot of `appConfig/default` (site intake + published folders). */
data class AppConfigStatus(
    val sitePublished: Boolean,
    val activeFolderId: String,
    val publishedFolderIds: List<String> = emptyList(),
) {
    val resolvedPublishedFolderIds: List<String>
        get() = PublishedFolderIds.resolve(publishedFolderIds, activeFolderId)
}
