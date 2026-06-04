# DroidKaigi 2026 Quiz

Compose Multiplatform クイズアプリ（Android + Desktop + Web/Wasm）。選択式・並び替え問題と当日ランキング（モック）に対応。

## 構成

- `composeApp` — 共有 UI（Nav3 + adaptive）
- `androidApp` — Android エントリ（参加者向け）
- `desktopApp` — Desktop エントリ（参加者向け）
- `staffComposeApp` / `staffDesktopApp` — **スタッフ用** Desktop（クイズ内容・ランキング確認、PC 運営向け）
- `wasmApp` — Web（Wasm）エントリ
- `core:domain` / `core:data` / `core:ui`
- `feature:quiz` / `feature:ranking` / `feature:staff`

仕様: [docs/SPEC.md](docs/SPEC.md) · AI 向け: [AGENTS.md](AGENTS.md)

## ランタイムバリアント（fake / prod）

データ層は **2 つのランタイム** を持ち、ビルド時にどちらか一方だけがコンパイルされます（`src/fakeMain` または `src/prodMain` を `commonMain` に載せ替え + Metro グラフ切り替え）。

| バリアント | `quiz.runtime` | 内容 |
|------------|----------------|------|
| **fake**（デフォルト） | `fake` | 同梱 JSON（`quiz_set.json`）とローカルランキング。オフライン・会場デモ向け。 |
| **prod** | `prod` | サーバー接続用（`RemoteQuizRepository` / `RemoteRankingRepository`）。Firebase 等は `core/data/src/prodMain` に実装。 |

### 切り替え方

**永続的に変える** — ルートの [gradle.properties](gradle.properties):

```properties
quiz.runtime=fake
# quiz.runtime=prod
```

**1 回だけ上書き** — Gradle プロパティ:

```bash
./gradlew -Pquiz.runtime=prod ...
```

`quiz.runtime` を変更したあとは、**必ず再ビルド**してください（選ばれていない側の source set はコンパイルされません）。

### ビルド・実行例

Android（fake・既定）:

```bash
./gradlew :androidApp:assembleDebug
# Android Studio から androidApp を Run でも可
```

Android（prod）:

```bash
./gradlew :androidApp:assembleDebug -Pquiz.runtime=prod
```

Desktop（fake）:

```bash
./gradlew :desktopApp:run
```

Desktop（prod）:

```bash
./gradlew :desktopApp:run -Pquiz.runtime=prod
```

スタッフ用 Desktop（**常に fake** — 同梱 JSON + インメモリランキング）:

```bash
./gradlew :staffDesktopApp:run
```

参加者アプリとは別プロセスのため、fake ランキングはプロセス内メモリのみ（デモ3件 + そのセッション中の操作）。会場で参加者スコアと同期するには将来 `prod` API 等が必要。詳細は [docs/VERIFY.md](docs/VERIFY.md)。

テスト（fake 向けの data テストは `quiz.runtime=fake` のときのみ有効）:

```bash
./gradlew :core:data:jvmTest
./gradlew :core:data:jvmTest -Pquiz.runtime=prod   # prod では Fake 専用テストは除外
```

## ビルド

AGP 9.x + Gradle 9.4。Android アプリは `:androidApp` モジュールです。ランタイムの詳細は上記「ランタイムバリアント」を参照。

```bash
./gradlew :androidApp:assembleDebug
```

## テスト

```bash
./gradlew :core:domain:jvmTest :core:data:jvmTest
./gradlew :androidApp:connectedDebugAndroidTest  # 要エミュレータ
```

## Desktop

参加者向け:

```bash
./gradlew :desktopApp:run
```

## スタッフ用 Desktop

運営 PC 向け。NavigationRail でクイズ（問題・正解の一覧）と本日のランキングを表示。ログインなし・`quiz.runtime` は常に fake（`staffComposeApp` の Metro グラフ）。

```bash
./gradlew :staffDesktopApp:run
```

## Web (Wasm)

Chrome 119+ など Wasm GC 対応ブラウザが必要です。

```bash
./gradlew :wasmApp:wasmJsBrowserDevelopmentRun
```

## 手動確認

[docs/VERIFY.md](docs/VERIFY.md)
