package com.droidkaigi.quiz

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Persists Japanese locale preference before the Activity launches so UI assertions
 * stay stable on English emulators.
 */
class ForceJapaneseLocaleRule : TestRule {
    override fun apply(base: Statement, description: Description): Statement =
        object : Statement() {
            override fun evaluate() {
                val context = InstrumentationRegistry.getInstrumentation().targetContext
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString(KEY_LOCALE, "ja")
                    .commit()
                base.evaluate()
            }
        }

    private companion object {
        const val PREFS_NAME = "quiz_locale"
        const val KEY_LOCALE = "locale"
    }
}
