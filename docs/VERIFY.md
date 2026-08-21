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

- **認証**（fake）: メール `staff@droidkaigi.local`、パスワード `staff2026`（ローカル固定値・開発専用）で入力ログイン、またはログインボタン下の「デモアカウントでログイン」でワンクリック。成功後にコンソールへ遷移。トップバーの「ログアウト」で認証画面に戻る。prod ではワンクリックボタンは出ない
- **スクショ（JVM UI テスト）**: `xvfb-run ./gradlew :feature:staff:jvmTest --tests 'jp.co.yumemi.quiz.droidkaigi.feature.staff.auth.StaffAuthContentJvmUiTest'` → `docs/screenshots/staff/staff-auth-fake-quick-login.png`（および prod 相当の非表示版）。全体キャプチャは `StaffScreenshotJvmUiTest`（`docs/screenshots/staff/`）

**本番（prod）**

```bash
./gradlew :staffDesktopApp:run -Pquiz.runtime=prod
```

[Firebase セットアップ](DEVELOPMENT.md#firebase-セットアップ) のスタッフ用ログインで認証する。

**DMG 用 jlink ランタイムの確認（`run` では検出できない不足モジュール向け）**

1. **ヒント（静的解析・不完全）** — `jdeps` ベースの提案を表示:

   ```bash
   ./gradlew :staffDesktopApp:suggestRuntimeModules -Pquiz.runtime=prod
   ```

   出力の `modules(...)` と `gradle/desktop-jlink-modules.gradle.kts` を突き合わせる。Firestore 経由の `java.sql` などは拾えないことがある。

2. **確実なスモーク** — パッケージと同じ最小 JRE で起動:

   ```bash
   ./gradlew :staffDesktopApp:runDistributable -Pquiz.runtime=prod
   ```

   ログイン → フォルダ一覧まで通れば DMG でも同様に動く想定。

3. **同梱モジュール一覧** — ビルド後:

   ```bash
   ./gradlew :staffDesktopApp:createRuntimeImage -Pquiz.runtime=prod -q
   cat staffDesktopApp/build/compose/tmp/main/runtime/release
   ```

   `MODULES="..."` に `java.sql` / `jdk.unsupported` 等が含まれるか確認。

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
