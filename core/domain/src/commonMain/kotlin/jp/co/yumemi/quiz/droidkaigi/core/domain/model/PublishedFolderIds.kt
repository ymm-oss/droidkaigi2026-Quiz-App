package jp.co.yumemi.quiz.droidkaigi.core.domain.model

/** Resolves participant-visible folder IDs from appConfig, including legacy single-id docs. */
object PublishedFolderIds {
    fun resolve(publishedFolderIds: List<String>, activeFolderId: String = ""): List<String> {
        val fromList = publishedFolderIds.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        if (fromList.isNotEmpty()) return fromList
        return listOfNotNull(activeFolderId.trim().takeIf { it.isNotEmpty() })
    }
}
