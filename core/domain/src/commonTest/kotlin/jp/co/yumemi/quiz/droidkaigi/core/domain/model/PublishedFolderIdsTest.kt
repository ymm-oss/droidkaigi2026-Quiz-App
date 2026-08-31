package jp.co.yumemi.quiz.droidkaigi.core.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class PublishedFolderIdsTest {
    @Test
    fun prefersPublishedListOverActiveFolderId() {
        assertEquals(
            listOf("easy", "hard"),
            PublishedFolderIds.resolve(
                publishedFolderIds = listOf("easy", " hard ", "easy"),
                activeFolderId = "legacy",
            ),
        )
    }

    @Test
    fun fallsBackToActiveFolderIdWhenListEmpty() {
        assertEquals(
            listOf("legacy"),
            PublishedFolderIds.resolve(publishedFolderIds = emptyList(), activeFolderId = "legacy"),
        )
        assertEquals(
            emptyList(),
            PublishedFolderIds.resolve(publishedFolderIds = listOf(" ", ""), activeFolderId = ""),
        )
    }
}
