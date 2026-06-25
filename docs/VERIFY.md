# Manual verification (Android CLI)

## Runtime (`fake` / `prod`)

Set in `gradle.properties` (or override per build):

```properties
quiz.runtime=fake   # dev only: bundled JSON + in-memory ranking (default)
# quiz.runtime=prod # production: remote quiz + ranking required (Firebase etc.)
```

**本番（prod）**: 問題・ランキングはオンライン必須。接続失敗時はエラー（fake へフォールバックしない）。

**開発（fake）**: ネットなしで参加者フロー（Home → Quiz → Result → Ranking）を手動確認できる。

```bash
./gradlew :androidApp:assembleProdDebug
```

Android Studio: Build Variants → `prodDebug`（または `fakeDebug`）。KMP は 1 回の Gradle 実行で 1 ランタイムのみ（`assembleFakeDebug` と `assembleProdDebug` を同時に並べない）。

Rebuild after changing runtime (inactive `fakeMain` / `prodMain` is not compiled).

## Staff desktop

`quiz.runtime` は参加者アプリと同じプロパティで、`staffComposeApp` と `core:data` の fake/prod が連動します。

**開発（fake・インメモリ）**

```bash
./gradlew :staffDesktopApp:run
```

- **認証**（fake）: メール `staff@droidkaigi.local`、パスワード `staff2026`（ローカル固定値・開発専用）。成功後にコンソールへ遷移。トップバーの「ログアウト」で認証画面に戻る

**本番（prod）**

```bash
./gradlew :staffDesktopApp:run -Pquiz.runtime=prod
```

Firebase プロジェクトは**準備中**。

## Prerequisites

- Android SDK and emulator or device
- `android` CLI on PATH

## Steps

1. Build: `./gradlew :androidApp:assembleDebug`
2. Install & run: `android run --path androidApp/build/outputs/apk/debug/androidApp-debug.apk` (or run **androidApp** from Android Studio)
3. Flow: Home → enter nickname → Quiz (3 questions) → Result → Ranking
4. Screenshot: `android screenshot --output docs/screenshots/flow-$(date +%Y%m%d).png`
5. On UI issues: `android layout` for hierarchy JSON

## Required screenshots (release gate)

- `docs/screenshots/home.png`
- `docs/screenshots/quiz.png`
- `docs/screenshots/result.png`
- `docs/screenshots/ranking.png`
