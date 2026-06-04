package com.droidkaigi.quiz.core.data

import com.droidkaigi.quiz.core.data.di.AppScope
import com.droidkaigi.quiz.core.data.dto.QuizSetDto
import com.droidkaigi.quiz.core.data.dto.toDomain
import com.droidkaigi.quiz.core.data.generated.resources.Res
import com.droidkaigi.quiz.core.domain.model.QuizSet
import com.droidkaigi.quiz.core.domain.repository.QuizRepository
import dev.zacsweers.metro.ContributesBinding
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

@ContributesBinding(AppScope::class)
class JsonQuizRepository(
    private val json: Json = Json { ignoreUnknownKeys = true },
) : QuizRepository {
    @OptIn(ExperimentalResourceApi::class)
    override suspend fun getDefaultQuizSet(): QuizSet {
        val bytes = Res.readBytes("files/quiz_set.json")
        val text = bytes.decodeToString()
        return json.decodeFromString<QuizSetDto>(text).toDomain()
    }
}
