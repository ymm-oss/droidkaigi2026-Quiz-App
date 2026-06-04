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
./gradlew :androidApp:assembleDebug -Pquiz.runtime=prod
```

Rebuild after changing runtime (inactive `fakeMain` / `prodMain` is not compiled).

## Staff desktop

`quiz.runtime` は参加者アプリと同じプロパティで、`staffComposeApp` と `core:data` の fake/prod が連動します。

**開発（fake・インメモリ）**

```bash
./gradlew :staffDesktopApp:run
```

- **認証**（fake）: メール `staff@droidkaigi.local`、パスワード `staff2026`（ローカル固定値・開発専用）。成功後にコンソールへ遷移。トップバーの「ログアウト」で認証画面に戻る

**本番（prod・Firebase Auth + Firestore 予定）**

```bash
./gradlew :staffDesktopApp:run -Pquiz.runtime=prod
```

- **認証**: `ProdStaffAuthRepository`（Firebase Authentication 実装予定）
- **データ**: `RemoteQuizCatalogRepository` 等（参加者 prod と同じ Firestore。スタッフは書き込み権限付き）
- **フォルダ**: 左ペインで選択・追加。「参加者向けに公開」で参加者アプリのクイズ／ランキング対象を切り替え
- **クイズ**: 問題の追加・編集・削除、正解と解説（Markdown: `**太字**`, `` `code` ``, `- 箇条書き`, `## 見出し`）
- **ランキング**（fake）: 選択フォルダの当日スコア（インメモリ。別プロセスの参加者アプリとは共有されない）
- **ランキング**（prod）: Firestore `folders/{folderId}/rankings`（参加者アプリと同一データ）

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
