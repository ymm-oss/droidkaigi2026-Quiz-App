package com.droidkaigi.quiz.core.data.di

import com.droidkaigi.quiz.core.data.StaffAuthHolder
import com.droidkaigi.quiz.core.data.firestore.FirestoreBootstrap
import com.droidkaigi.quiz.core.data.firestore.FirestoreService
import com.droidkaigi.quiz.core.data.firestore.createFirestoreService
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
@BindingContainer
object ProdDataBindings {
    @Provides
    @SingleIn(AppScope::class)
    fun provideFirestoreService(staffAuthHolder: StaffAuthHolder): FirestoreService {
        FirestoreBootstrap.ensureInitialized()
        return createFirestoreService(staffAuthHolder)
    }
}
