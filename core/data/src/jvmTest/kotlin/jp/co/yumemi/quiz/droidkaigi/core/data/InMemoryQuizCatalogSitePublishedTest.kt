package jp.co.yumemi.quiz.droidkaigi.core.data

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizFolder
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizSet
import jp.co.yumemi.quiz.droidkaigi.core.domain.time.SystemInstantProvider
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InMemoryQuizCatalogSitePublishedTest {
    @Test
    fun sitePublished_defaultsFalse_andSurvivesActiveFolderChange() = runBlocking {
        val catalog = InMemoryQuizCatalog()
        catalog.withLock {
            assertFalse(getSitePublished())
            val folder = createFolder("Day 1", "")
            setSitePublished(true)
            assertTrue(getSitePublished())
            setActiveFolderId(folder.id)
            assertTrue(getSitePublished())
            setSitePublished(false)
            assertFalse(getSitePublished())
        }
    }

    @Test
    fun repository_setActiveFolderId_preservesSitePublished() = runBlocking {
        val catalog = InMemoryQuizCatalog()
        val repo = InMemoryQuizCatalogRepository(catalog, SystemInstantProvider())
        catalog.withLock {
            seedFolder(
                folder = QuizFolder(id = "a", name = "A", sortOrder = 0),
                quizSet = QuizSet(id = "a", title = "A", questions = emptyList()),
            )
            seedFolder(
                folder = QuizFolder(id = "b", name = "B", sortOrder = 1),
                quizSet = QuizSet(id = "b", title = "B", questions = emptyList()),
            )
            setSitePublished(true)
        }
        repo.setActiveFolderId("b")
        assertTrue(repo.getSitePublished())
        assertEquals("b", repo.getActiveFolderId())
    }
}
