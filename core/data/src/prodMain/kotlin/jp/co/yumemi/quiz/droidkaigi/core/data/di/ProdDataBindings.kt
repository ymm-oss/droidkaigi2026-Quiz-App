package jp.co.yumemi.quiz.droidkaigi.core.data.di

import jp.co.yumemi.quiz.droidkaigi.core.data.StaffAuthHolder
import jp.co.yumemi.quiz.droidkaigi.core.data.firestore.FirestoreBootstrap
import jp.co.yumemi.quiz.droidkaigi.core.data.firestore.FirestoreService
import jp.co.yumemi.quiz.droidkaigi.core.data.firestore.createFirestoreService
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
