# 開発環境・ビルド

初めて触る方は先に [CONTRIBUTING.md](CONTRIBUTING.md)（参加方法・PR・レビュー）を読んでください。

## リポジトリ構成

- `composeApp` — 共有 UI（Nav3 + adaptive）
- `androidApp` — Android エントリ（参加者向け）
- `desktopApp` — Desktop エントリ（参加者向け）
- `staffComposeApp` / `staffDesktopApp` — **スタッフ用** Desktop（クイズ内容・ランキング確認、PC 運営向け）
- `wasmApp` — Web（Wasm）エントリ（未運用）
- `core:domain` / `core:data` / `core:ui`
- `feature:quiz` / `feature:ranking` / `feature:staff`

## ランタイムバリアント（fake / prod / local）

データ層は **fake** と **prod 系（prod / local）** の 2 実装を持ち、ビルド時にどちらか一方だけがコンパイルされます（`src/fakeMain` または `src/prodMain` を `commonMain` に載せ替え + Metro グラフ切り替え）。`local` は `prodMain` を使い、接続先だけ Firebase Emulator に向けます。

| バリアント | `quiz.runtime` | 内容 |
|------------|----------------|------|
| **fake**（デフォルト） | `fake` | **開発専用**: 同梱 [quiz_set.json](../core/data/src/commonMain/composeResources/files/quiz_set.json) とインメモリランキング。ネット不要で UI・採点を検証。 |
| **prod** | `prod` | **本番**: 問題・ランキングとも Firestore 必須（`RemoteQuizCatalogRepository` / `RemoteRankingRepository` 等）。`core/data/src/prodMain` に実装。オフライン非対応。 |
| **local** | `local` | **エミュレータ**: prod と同じ `prodMain` + Firebase Emulator Suite（Firestore 8080 / Auth 9099）。クラウド誤書き込みを避けつつ prod 結合を検証。 |

### 全体像（fake / prod と Firebase）

`quiz.runtime` で **データ層だけ** が切り替わります。UI モジュール（参加者・スタッフ）は共通で、ビルド時に fake 用 / prod 用の Repository が載せ替わります。

```mermaid
flowchart TB
  subgraph fake["quiz.runtime = fake（開発・オフライン）"]
    direction TB
    FJSON["同梱 quiz_set.json<br/>（問題データ）"]
    FMEM["インメモリ<br/>（ランキング・カタログ）"]
    FAUTH["デモ用スタッフ認証<br/>（インメモリ）"]

    subgraph fakeApps["アプリ（Firebase 不使用）"]
      FA["androidApp"]
      FD["desktopApp"]
      FS_APP["staffDesktopApp"]
    end

    FA --> FJSON
    FA --> FMEM
    FD --> FJSON
    FD --> FMEM
    FS_APP --> FAUTH
    FS_APP --> FMEM
  end

  subgraph prod["quiz.runtime = prod（本番・会場）"]
    direction TB
    subgraph firebase["Firebase プロジェクト"]
      FSDB[("Firestore<br/>folders / appConfig<br/>rankings")]
      AUTH["Firebase Authentication<br/>（スタッフのみ）"]
    end

    subgraph prodParticipant["参加者アプリ（問題取得・スコア送信）"]
      PA["androidApp"]
      PD["desktopApp"]
    end

    STAFF["staffDesktopApp<br/>（問題管理・公開切替）"]

    PA -->|"読取: 公開中クイズ・ランキング"| FSDB
    PA -->|"書込: スコア"| FSDB
    PD -->|"読取: 公開中クイズ・ランキング"| FSDB
    PD -->|"書込: スコア"| FSDB

    STAFF -->|"ログイン"| AUTH
    STAFF -->|"編集: 問題・フォルダ・公開"| FSDB
    STAFF -->|"参照: ランキング"| FSDB
  end

  WASM["wasmApp<br/>（Web / Wasm）"]
  WASM -.->|"未使用（要検討）<br/>QR 配布など"| prodParticipant
```

要点:

| 観点 | fake | prod |
|------|------|------|
| **問題データ** | リポジトリ同梱 JSON | **Firestore** `folders/{folderId}` |
| **問題の編集** | スタッフアプリ → インメモリ（再起動で消える） | **スタッフアプリ** → Firebase Auth 後に Firestore へ保存 |
| **参加者アプリ** | Android / Desktop（ネット不要） | Android / Desktop（Firestore 必須） |
| **Wasm** | ビルド可能だが本番未採用 | 同上（要検討） |

### 初期データ（fake）

`quiz.runtime=fake` で使う問題データは、同梱の [quiz_set.json](../core/data/src/commonMain/composeResources/files/quiz_set.json) のみ。`FakeQuizCatalogSeeder` が起動時にインメモリ catalog へ投入する。ランキングもインメモリ（再起動で消える）。

[firestore-seed.json](firestore-seed.json) は、同じデモ問題を **Firestore ドキュメント形式**で示した参考 JSON（fake 実行時には読み込まない。prod の Firestore 仕様を理解するための対応表）。

### 切り替え方

| プラットフォーム | 切り替え |
|------------------|----------|
| **Android** | **Build Variant**（下記 [Android Build Variant](#android-build-variantruntime-flavor)） |
| **JVM（Desktop / スタッフ）** | [gradle.properties](../gradle.properties) の `quiz.runtime`、`-Pquiz.runtime=prod`、または Android Studio の Run Configuration（下記 [JVM（Desktop / スタッフ）](#jvmdesktop--スタッフ)） |

### JVM（Desktop / スタッフ）

参加者 Desktop（`:desktopApp`）とスタッフ Desktop（`:staffDesktopApp`）は JVM ターゲット。`quiz.runtime` は [gradle.properties](../gradle.properties) または `-Pquiz.runtime=prod` で切り替える。

**Android Studio（スタッフ Desktop）**

Run Configuration で切り替えて実行できる（`.run/staffDesktop[Fake].run.xml` / `.run/staffDesktop[Prod] .run.xml`）。

1. ツールバーの Run Configuration ドロップダウンを開く
2. **`staffDesktop[Fake]`**（fake）、**`staffDesktop[Local]`**（local / エミュ）、または **`staffDesktop[Prod]`**（prod）を選択
3. Run（`:staffDesktopApp:run` が実行される。Local は `-Pquiz.runtime=local`、Prod は `-Pquiz.runtime=prod` 付き）

`quiz.runtime` を変更したあとは、**必ず再ビルド**してください（選ばれていない側の source set はコンパイルされません）。

### Android Build Variant（`runtime` flavor）

参加者 Android（`:androidApp`）だけ **AGP の productFlavor** で fake / prod / local を切り替えます。KMP ライブラリ（`:composeApp` / `:core:data` など）は [Android-KMP プラグイン](https://developer.android.com/kotlin/multiplatform/plugin)の都合で **flavor を持たない**ため、同じビルド内の `quiz.runtime` は [gradle/quiz-runtime.gradle.kts](../gradle/quiz-runtime.gradle.kts) で 1 つに揃えます。

| Build Variant | productFlavor | `quiz.runtime`（KMP） | データ源 | パッケージ名（例） |
|---------------|---------------|----------------------|----------|-------------------|
| **fakeDebug**（既定） | `fake` | `fake` | 同梱 JSON + インメモリ | `com.droidkaigi.quiz.fake` |
| **prodDebug** | `prod` | `prod` | Firestore（クラウド） | `com.droidkaigi.quiz` |
| **localDebug** | `local` | `local` | Firestore（**エミュレータ**） | `com.droidkaigi.quiz.local` |
| fakeRelease / prodRelease / localRelease | 同上 | 同上 | 同上 | 同上 |

`quiz.runtime` の決まり方（優先順）:

1. Gradle タスク名に含まれる flavor（`assembleProdDebug` → `prod`、`assembleLocalDebug` → `local`）
2. `-Pquiz.runtime=…` または [gradle.properties](../gradle.properties)
3. 既定 `fake`

そのため **`gradle.properties` が `quiz.runtime=fake` のままでも、`prodDebug` をビルドすれば KMP は prod** になります（逆に、Variant を prod にしても Gradle Sync だけでは KMP が fake のまま、ということはありません。**インストールする APK を prodDebug でビルドしたか**が重要です）。

**Android Studio の手順**

1. **View → Tool Windows → Build Variants**
2. モジュール `:androidApp` を **fakeDebug** / **localDebug** / **prodDebug** に変更
3. **Build → Rebuild Project**（Variant 切替後は必須）
4. Run 設定 [`.run/androidApp.run.xml`](../.run/androidApp.run.xml) などで `:androidApp` を実行

**Firebase プロジェクト（prod）**: [Firebase セットアップ](#firebase-セットアップ) を完了してから `prodDebug` / `-Pquiz.runtime=prod` で結合確認する。

**注意**

- fake / prod / local の APK は **別アプリ**として端末に共存可能（applicationId が異なる）
- `./gradlew :androidApp:assembleFakeDebug :androidApp:assembleProdDebug` のように **1 コマンドで複数 flavor を並べると KMP は fake にフォールバック**する。片方ずつビルドする
- prod なのにデモ問題が出る → **fakeDebug の APK が入っている**か、Rebuild 不足。ログに `quiz.runtime resolved to 'prod'` が出るか確認
- local なのにクラウドに書き込む → **localDebug** をビルドしているか確認。ログに `useFirebaseEmulator=true` が出ること

### Firebase Emulator（`local`）

prod と同じ Repository 経路で **ローカルエミュレータ**に接続する。`firebase.json` に emulators 設定済み。  
日常の起動は **`--import=./emulator-data`**（リポジトリ同梱のスナップショット）。問題データの参考 JSON は [firestore-seed.json](firestore-seed.json)。

#### 前提

- [Firebase CLI](https://firebase.google.com/docs/cli): `npm install -g firebase-tools`
- リポジトリの [emulator-data/](../emulator-data/)

#### エミュレータの起動

リポジトリルート:

```bash
firebase emulators:start --only auth,firestore --import=./emulator-data
```

| 項目 | 値 |
|------|-----|
| Emulator UI | http://localhost:4000 |
| Auth | `9099` |
| Firestore | `8080` |
| 初期データ | `emulator-data/`（`--import`） |

停止は Ctrl+C。

スタッフ用 Auth ユーザーは Emulator UI → Authentication で 1 件追加する（参加者アプリのクイズ開始には不要。スタッフ Desktop の local ログインに必要）。セッション中に作ったユーザーを baseline に残したい場合は、停止前に次を実行する（共有 `emulator-data/` を上書きするので注意）:

```bash
firebase emulators:export ./emulator-data --force
```

#### アプリ実行（エミュレータ起動後）

```bash
./gradlew :androidApp:assembleLocalDebug
./gradlew :staffDesktopApp:run -Pquiz.runtime=local
./gradlew :desktopApp:run -Pquiz.runtime=local
```

初期データが無い場合はクイズを開始できない（prod でも同梱 JSON へのフォールバックはしない）。

Android エミュレータから接続する場合、Firestore / Auth ホストは `10.0.2.2`（ホスト PC）。実機の場合は `adb reverse tcp:8080 tcp:8080` と `adb reverse tcp:9099 tcp:9099` を検討（ホストは `127.0.0.1` 側に合わせる必要あり）。

**使い分け**: 日常 UI → `fake` / Firestore 結合・ルール検証 → `local` / 会場・本番 → `prod`

**永続的に変える（Desktop など）** — ルートの `gradle.properties`:

```properties
quiz.runtime=fake
# quiz.runtime=prod
# quiz.runtime=local
```

**1 回だけ上書き**:

```bash
./gradlew -Pquiz.runtime=prod ...
```

Desktop / Wasm では上記 [切り替え方](#切り替え方) の `gradle.properties` または `-Pquiz.runtime` を使う。スタッフ Desktop は Android Studio では [JVM（Desktop / スタッフ）](#jvmdesktop--スタッフ) の Run Configuration でも切り替え可能。

## Firebase セットアップ

`quiz.runtime=prod` でビルド・実行するときに、**開発者が手元で用意するもの**。Firestore の仕様は [FIRESTORE.md](FIRESTORE.md)。

### 用意するもの

| 項目 | 内容 |
|------|------|
| **Firebase プロジェクトへのアクセス** | [.firebaserc](../.firebaserc) のプロジェクト（本番: `droidkaigi26`）への権限 |
| **`google-services.json`** | 下記パスに配置（Android `prod`・Desktop / スタッフ Desktop 共通） |
| **スタッフ用ログイン** | prod 用メール / パスワード（運営から共有。fake の `staff@droidkaigi.local` は使えない） |

### `google-services.json`

配置先は **1 か所のみ**:

```
androidApp/src/prod/google-services.json
```

[Firebase Console](https://console.firebase.google.com/) の **プロジェクト設定 → マイアプリ → Android（`com.droidkaigi.quiz`）** から **google-services.json** をダウンロードし、上記パスに置く。リポジトリに同梱されている場合はそのまま使える。フィールド構成の参考: [google-services.json.example](../androidApp/src/prod/google-services.json.example)。

### リポジトリ内の Firebase ファイル

| パス | 内容 |
|------|------|
| [.firebaserc](../.firebaserc) | CLI のデフォルトプロジェクト ID |
| [firebase.json](../firebase.json) | Firestore ルール・インデックス、Wasm 向け Hosting（Hosting は未デプロイ） |
| [firestore.rules](../firestore.rules) / [firestore.indexes.json](../firestore.indexes.json) | ルール・インデックス定義 |
| [functions/](../functions/) | Cloud Functions 雛形（**未使用**・`firebase.json` 登録済み） |
| [docs/firestore-seed.json](firestore-seed.json) | Emulator / Firestore 向け問題データの参考 JSON |
| [emulator-data/](../emulator-data/) | Emulator `--import` 用スナップショット |

### prod の起動

```bash
./gradlew :androidApp:assembleProdDebug
./gradlew :staffDesktopApp:run -Pquiz.runtime=prod
./gradlew :desktopApp:run -Pquiz.runtime=prod
```

結合確認: [VERIFY.md](VERIFY.md)

**環境の切り分け**

- **開発**: `quiz.runtime=fake`（既定）— 同梱 JSON + インメモリランキングでオフライン検証。Firebase 不要。
- **結合（local）**: `quiz.runtime=local` または `localDebug` — Firebase Emulator（[Firebase Emulator（`local`）](#firebase-emulatorlocal)）
- **結合・会場（prod）**: 上記を用意したうえで Firestore + Firebase Auth（クラウド）を使用

Repository マッピング: [FIRESTORE.md#アプリからのマッピング](FIRESTORE.md#アプリからのマッピング)

## ビルド・実行

AGP 9.x + Gradle 9.4。Android アプリは `:androidApp` モジュール。

### 参加者 — Android

```bash
./gradlew :androidApp:assembleFakeDebug    # 開発（fake）
./gradlew :androidApp:assembleLocalDebug   # エミュレータ（local・要 [Emulator 起動](#firebase-emulatorlocal)）
./gradlew :androidApp:assembleProdDebug    # prod（要 [Firebase セットアップ](#firebase-セットアップ)）
./gradlew :androidApp:assembleDebug        # 既定 Variant に依存
```

Android Studio の Variant 手順は [Android Build Variant](#android-build-variantruntime-flavor) を参照。

### 参加者 — Desktop

```bash
./gradlew :desktopApp:run                                    # fake（既定・JDK 17+）
./gradlew :desktopApp:run -Pquiz.runtime=local               # エミュレータ（JDK 17+）
./gradlew :desktopApp:run -Pquiz.runtime=prod                # prod（JDK 17+）
```

### スタッフ — Desktop

```bash
./gradlew :staffDesktopApp:run                               # fake（既定・JDK 17+）
./gradlew :staffDesktopApp:run -Pquiz.runtime=local          # エミュレータ（JDK 17+）
./gradlew :staffDesktopApp:run -Pquiz.runtime=prod           # prod（JDK 17+）
```

Android Studio では Run Configuration **`staffDesktop[Fake]`** / **`staffDesktop[Local]`** / **`staffDesktop[Prod]`** の切り替えでも fake / local / prod を選べる（[JVM（Desktop / スタッフ）](#jvmdesktop--スタッフ)）。

- **fake**: デモログイン `staff@droidkaigi.local` / `staff2026`（インメモリ）。参加者アプリとは別プロセスのためランキングはプロセス内のみ。
- **local**: 先に [Emulator 起動](#firebase-emulatorlocal) → Emulator UI で作成したスタッフアカウントでログイン
- **prod**: [Firebase セットアップ](#firebase-セットアップ) のスタッフ用ログインで認証

### Web（Wasm）

Chrome 119+ など Wasm GC 対応ブラウザが必要。本番未採用（要検討）。

```bash
./gradlew :wasmApp:wasmJsBrowserDevelopmentRun
```

## テスト

```bash
./gradlew :core:domain:jvmTest :core:data:jvmTest
./gradlew :core:data:jvmTest -Pquiz.runtime=prod   # prod では Fake 専用テストは除外
./gradlew :androidApp:connectedDebugAndroidTest    # 要エミュレータ
```

## 手動確認

[VERIFY.md](VERIFY.md)（会場・prod 結合の確認手順を含む）
