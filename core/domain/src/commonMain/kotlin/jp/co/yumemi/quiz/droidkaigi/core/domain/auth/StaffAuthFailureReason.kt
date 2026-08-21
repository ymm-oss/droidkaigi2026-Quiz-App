package jp.co.yumemi.quiz.droidkaigi.core.domain.auth

/**
 * Staff sign-in failure categories shown to operators.
 * Raw Firebase / Identity Toolkit payloads must not reach the UI.
 */
enum class StaffAuthFailureReason {
    InvalidCredentials,
    InvalidEmail,
    UserDisabled,
    TooManyAttempts,
    Network,
    Unsupported,
    Unknown,
    ;

    fun userMessage(): String = when (this) {
        InvalidCredentials -> "メールアドレスまたはパスワードが正しくありません"
        InvalidEmail -> "メールアドレスの形式が正しくありません"
        UserDisabled -> "このアカウントは無効化されています。管理者に連絡してください"
        TooManyAttempts -> "試行回数が上限に達しました。しばらく時間をおいて再度お試しください"
        Network -> "ネットワークに接続できません。接続を確認してください"
        Unsupported -> "この環境ではスタッフ認証に未対応です。Desktop をご利用ください"
        Unknown -> "ログインに失敗しました"
    }
}
