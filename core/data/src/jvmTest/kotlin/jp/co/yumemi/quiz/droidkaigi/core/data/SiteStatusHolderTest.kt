package jp.co.yumemi.quiz.droidkaigi.core.data

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.AppConfigStatus
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizFolder
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizSet
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.QuizCatalogRepository
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.ObserveAppConfigUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

@OptIn(ExperimentalCoroutinesApi::class)
class SiteStatusHolderTest {
    @Test
    fun isRankingNavVisible_onlyWhenPublishedTrue() {
        val holder = SiteStatusHolder()
        assertFalse(holder.isRankingNavVisible)

        holder.updateSitePublished(null)
        assertFalse(holder.isRankingNavVisible)

        holder.updateSitePublished(false)
        assertFalse(holder.isRankingNavVisible)

        holder.updateSitePublished(true)
        assertTrue(holder.isRankingNavVisible)
    }

    @Test
    fun applyStatus_updatesPublishedAndFolder() {
        val holder = SiteStatusHolder()
        holder.applyStatus(AppConfigStatus(sitePublished = true, activeFolderId = "day2"))
        assertEquals(true, holder.sitePublished.value)
        assertEquals("day2", holder.activeFolderId.value)
        assertFalse(holder.observeFailed.value)
        assertTrue(holder.isRankingNavVisible)
    }

    @Test
    fun bindAppConfig_appliesEmissions() = runTest {
        val emissions = MutableSharedFlow<AppConfigStatus>(extraBufferCapacity = 8)
        val holder = SiteStatusHolder()
        val job = launch {
            holder.bindAppConfig(ObserveAppConfigUseCase(flowCatalog(emissions)))
        }
        runCurrent()

        emissions.emit(AppConfigStatus(sitePublished = false, activeFolderId = "a"))
        runCurrent()
        assertEquals(false, holder.sitePublished.value)
        assertEquals("a", holder.activeFolderId.value)

        emissions.emit(AppConfigStatus(sitePublished = true, activeFolderId = "b"))
        runCurrent()
        assertEquals(true, holder.sitePublished.value)
        assertEquals("b", holder.activeFolderId.value)
        job.cancel()
    }

    @Test
    fun bindAppConfig_marksFailedOnError() = runTest {
        val holder = SiteStatusHolder()
        holder.bindAppConfig(
            ObserveAppConfigUseCase(
                flowCatalog(flow { error("network down") }),
            ),
        )
        assertTrue(holder.observeFailed.value)
        assertNull(holder.sitePublished.value)
    }

    @Test
    fun bindAppConfig_timesOutWhenNoFirstSnapshot() = runTest {
        val holder = SiteStatusHolder()
        val job = launch {
            holder.bindAppConfig(
                ObserveAppConfigUseCase(flowCatalog(flow { awaitCancellation() })),
            )
        }
        runCurrent()
        assertNull(holder.sitePublished.value)
        assertFalse(holder.observeFailed.value)
        advanceTimeBy(16_000L)
        runCurrent()
        assertTrue(holder.observeFailed.value)
        job.cancel()
    }

    private fun flowCatalog(emissions: Flow<AppConfigStatus>) = object : UnusedCatalog() {
        override fun observeAppConfig(): Flow<AppConfigStatus> = emissions
    }

    private open class UnusedCatalog : QuizCatalogRepository {
        override suspend fun listFolders(): List<QuizFolder> = fail("unused")
        override suspend fun createFolder(name: String, description: String): QuizFolder = fail("unused")
        override suspend fun updateFolder(folder: QuizFolder) = fail("unused")
        override suspend fun deleteFolder(folderId: String) = fail("unused")
        override suspend fun getQuizSet(folderId: String): QuizSet = fail("unused")
        override suspend fun saveQuizSet(quizSet: QuizSet) = fail("unused")
        override suspend fun getActiveFolderId(): String = fail("unused")
        override suspend fun setActiveFolderId(folderId: String) = fail("unused")
        override suspend fun getSitePublished(): Boolean = fail("unused")
        override suspend fun setSitePublished(published: Boolean) = fail("unused")
        override fun observeAppConfig(): Flow<AppConfigStatus> = emptyFlow()
    }
}
