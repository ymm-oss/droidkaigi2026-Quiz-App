package jp.co.yumemi.quiz.droidkaigi.core.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class QuizFolderTest {
    @Test
    fun listing_usesSeparatePublicInformation() {
        val folder = QuizFolder(
            id = "day1",
            name = "Day 1 internal",
            description = "ops note",
            publicName = "一般向け",
            publicDescription = "初級クイズ",
        )

        assertEquals("一般向け", folder.listingName)
        assertEquals("初級クイズ", folder.listingDescription)
    }

    @Test
    fun listing_usesInternalInformationWithoutDeletingStoredPublicInformation() {
        val folder = QuizFolder(
            id = "day1",
            name = "Day 1 internal",
            description = "ops note",
            publicName = "一般向け",
            publicDescription = "初級クイズ",
            useInternalAsPublic = true,
        )

        assertEquals("Day 1 internal", folder.listingName)
        assertEquals("ops note", folder.listingDescription)
        assertEquals("一般向け", folder.publicName)
        assertEquals("初級クイズ", folder.publicDescription)
    }

    @Test
    fun legacyFolder_fallsBackToInternalName() {
        val folder = QuizFolder(id = "legacy", name = "既存フォルダ", description = "内部説明")

        assertEquals("既存フォルダ", folder.listingName)
        assertEquals("内部説明", folder.listingDescription)
    }
}
