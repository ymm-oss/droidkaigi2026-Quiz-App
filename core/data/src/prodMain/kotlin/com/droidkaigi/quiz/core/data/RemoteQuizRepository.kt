package com.droidkaigi.quiz.core.data

import com.droidkaigi.quiz.core.data.di.AppScope
import com.droidkaigi.quiz.core.domain.model.QuizSet
import com.droidkaigi.quiz.core.domain.repository.QuizCatalogRepository
import com.droidkaigi.quiz.core.domain.repository.QuizRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(AppScope::class)
class RemoteQuizRepository(
    private val quizCatalogRepository: QuizCatalogRepository,
) : QuizRepository {
    override suspend fun getDefaultQuizSet(): QuizSet {
        val folderId = quizCatalogRepository.getActiveFolderId()
        return quizCatalogRepository.getQuizSet(folderId)
    }
}
