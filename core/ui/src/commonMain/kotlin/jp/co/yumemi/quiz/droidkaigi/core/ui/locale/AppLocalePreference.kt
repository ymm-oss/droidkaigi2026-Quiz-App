package jp.co.yumemi.quiz.droidkaigi.core.ui.locale

enum class AppLocalePreference {
    System,
    Japanese,
    English,
    ;

    val localeTag: String?
        get() = when (this) {
            System -> null
            Japanese -> "ja"
            English -> "en"
        }

    companion object {
        fun fromStorageKey(key: String?): AppLocalePreference = when (key) {
            "ja" -> Japanese
            "en" -> English
            else -> System
        }
    }

    val storageKey: String
        get() = when (this) {
            System -> "system"
            Japanese -> "ja"
            English -> "en"
        }
}
