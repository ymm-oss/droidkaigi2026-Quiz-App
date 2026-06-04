package com.droidkaigi.quiz.core.data

import com.droidkaigi.quiz.core.data.di.AppScope
import com.droidkaigi.quiz.core.domain.model.QuizSet
import com.droidkaigi.quiz.core.domain.repository.QuizRepository
import dev.zacsweers.metro.ContributesBinding

/**
 * Prod quiz source (e.g. Firebase Remote Config / Firestore). Wire the client SDK here.
 */
@ContributesBinding(AppScope::class)
class RemoteQuizRepository : QuizRepository {
    override suspend fun getDefaultQuizSet(): QuizSet {
        error(
            "RemoteQuizRepository is not implemented. " +
                "Connect Firebase or your API in :core:data prodMain.",
        )
    }
}
