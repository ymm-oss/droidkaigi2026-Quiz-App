# DroidKaigi 2026 Quiz

Compose Multiplatform クイズアプリ（Android + Desktop + Web/Wasm）。選択式・並び替え問題と当日ランキングに対応。本番は Firestore 等のリモート必須。開発時のみ `fake` でオフライン検証可。

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
| **fake**（デフォルト） | `fake` | **開発専用**: 同梱 JSON（`quiz_set.json`）とインメモリランキング。ネット不要で UI・採点を検証。 |
| **prod** | `prod` | **本番**: 問題・ランキングともリモート必須（`RemoteQuizRepository` / `RemoteRankingRepository` 等）。`core/data/src/prodMain` に実装。オフライン非対応。 |

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

### prod と Firestore（問題 DB・ランキング）

`quiz.runtime=prod` では参加者アプリが **Firestore** からクイズとランキングを読み書きする想定です（実装は `core/data/src/prodMain` の `RemoteQuizCatalogRepository` / `RemoteRankingRepository` など。現状はスタブ）。

| 用途 | fake（開発・デモ） | prod（本番） |
|------|-------------------|--------------|
| 参加者 Android / Desktop / Wasm | 同梱 JSON + プロセス内ランキング | Firestore（読み取り + スコア送信） |
| スタッフ Desktop（`staffDesktopApp`） | インメモリ認証・カタログ（ローカル固定値） | **Firebase Authentication** + Firestore（フォルダ編集・公開・ランキング参照） |

スタッフも参加者と同じ `quiz.runtime` で切り替えます（`staffComposeApp` の Metro グラフと `core:data` の fake/prod が連動）。**fake はあくまで開発用**、会場本番の運営 PC は `prod` を想定しています。

#### 推奨 Firestore データ形状

ドメイン（`QuizFolder` / `QuizSet` / `Question`）および同梱 JSON（`quiz_set.json`）に合わせ、**フォルダ ID = クイズセット ID** の 1:1 を維持する構成を推奨します。

```
folders/{folderId}                    # フォルダ + 問題一式（参加者はここを 1 回読む）
  name: string
  description: string
  sortOrder: number
  title: string                        # QuizSet.title（表示名）
  questions: array<map>              # 下記フィールド。並びは出題順
    type: "single_choice" | "multiple_choice" | "reorder"
    id, prompt, explanationMarkdown?
    options? / correctId? / correctIds? / items? / correctOrder?
  updatedAt: timestamp

appConfig/default                     # シングルトン（ドキュメント ID 固定）
  activeFolderId: string              # 「参加者向けに公開」中のフォルダ
  updatedAt: timestamp

folders/{folderId}/rankings/{entryId}  # スコア送信（サブコレクション）
  nickname: string
  score: number
  completedAtEpochMillis: number
  dateKey: string                      # 例 "2026-06-04"（当日 Top N 用。端末 TZ は InstantProvider に合わせる）
```

**この形を選ぶ理由**

- 参加者起動時は `getActiveFolderId` → `getQuizSet` の **2 読み取り**で足り、既存の `GetDefaultQuizSetUseCase` と一致する。
- `questions` は `QuizSetDto` / `quiz_set.json` と同型のため、シードや移行が容易。
- 問題数は会場想定で数十程度のため、1 ドキュメントに配列を載せても 1 MiB 制限に余裕がある（スタッフが全問まとめて保存する `SaveQuizSetUseCase` とも相性が良い）。
- ランキングだけサブコレクションに分離し、参加者の `submitScore` でドキュメントが増えてもフォルダ本体を肥大化させない。

**将来、問題単位の差分更新や同時編集が必要になったら**

`folders/{folderId}/questions/{questionId}` サブコレクションへ分割し、`sortOrder` フィールドで並びを管理する。初版は配列のままで十分。

**避けたい形（参考）**

- 全問題を 1 つの巨大 `quizSets/global` にだけ寄せる → フォルダ切替・スタッフ運用と `QuizCatalogRepository` の API がずれる。
- ランキングをフォルダドキュメント内の配列にのみ保持 → 当日の提出が増えると競合・サイズ増大。

#### Firebase プロジェクトの設定手順

1. [Firebase Console](https://console.firebase.google.com/) でプロジェクトを作成する。
2. **Firestore Database** を作成する（本番は **本番モード** で開始し、下記ルールをすぐ適用。テスト用に全開放ルールのままにしない）。
3. **Android アプリを登録**（パッケージ名 `com.droidkaigi.quiz` = `:androidApp` の `applicationId`）。
4. ダウンロードした `google-services.json` を **`androidApp/google-services.json`** に置く（リポジトリには含めない。`.gitignore` 済み想定）。
5. Android 側で Firebase / Firestore SDK を有効化する（未導入の場合の例）:
   - ルート `build.gradle.kts` に Google Services プラグイン
   - `:androidApp` に `com.google.gms.google-services` と `firebase-firestore`（または KMP 向け [GitLive firebase-kotlin-sdk](https://github.com/GitLiveApp/firebase-kotlin-sdk) を `core:data` の `prodMain` から利用）
6. **Desktop / Wasm** も prod で Firestore を使う場合は、同じプロジェクトの Web API キーまたは GitLive の各ターゲット設定が必要（プラットフォームごとに `prodMain` でクライアント初期化）。
7. Firestore に **複合インデックス**（Console のリンクから作成）:
   - コレクション `folders/{folderId}/rankings` — `dateKey` 昇順 + `score` 降順（当日ランキング取得用）

**初期データ例**（`folders/droidkaigi2026-demo` の `questions` は [quiz_set.json](core/data/src/commonMain/composeResources/files/quiz_set.json) をコピー、`appConfig/default` の `activeFolderId` を同じ ID にする）。

**セキュリティルール（例・要調整）**

会場公開クイズ＋匿名スコア送信を想定した出発点です。スタッフ書き込みをアプリから行う段階で **Firebase Authentication**（カスタムクレーム `staff` 等）を足してください。

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /folders/{folderId} {
      allow read: if true;
      allow write: if false; // 当面 Console / Admin SDK。将来 request.auth.token.staff == true
      match /rankings/{entryId} {
        allow read: if true;
        allow create: if request.resource.data.keys().hasAll(
            ['nickname', 'score', 'completedAtEpochMillis', 'dateKey'])
          && request.resource.data.nickname is string
          && request.resource.data.nickname.size() > 0
          && request.resource.data.nickname.size() <= 32
          && request.resource.data.score is int
          && request.resource.data.score >= 0;
        allow update, delete: if false;
      }
    }
    match /appConfig/{docId} {
      allow read: if true;
      allow write: if false;
    }
  }
}
```

**環境の切り分け**

- **開発**: `quiz.runtime=fake`（既定）— 同梱 JSON + インメモリランキングでオフライン検証。本番仕様の代替ではない。
- **結合・会場**: `-Pquiz.runtime=prod` + 検証用 Firebase プロジェクト（本番プロジェクトと分ける）。問題取得・ランキング表示・スコア送信はすべてネット必須。
- シークレット（`google-services.json`、API キー）は CI では暗号化シークレットや環境変数から生成し、コミットしない。

**実装メモ（コード側）**

- `RemoteQuizCatalogRepository` が上記 `folders` / `appConfig` をマッピングする。
- `RemoteQuizRepository.getDefaultQuizSet()` は `QuizCatalogRepository` のアクティブフォルダ経由でよい（`GetDefaultQuizSetUseCase` と同じ経路）。
- `RemoteRankingRepository` は `folders/{folderId}/rankings` を `dateKey` + `score` でクエリし、`InstantProvider` の「当日」と揃える。

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

スタッフ用 Desktop（**開発・既定は fake**）:

```bash
./gradlew :staffDesktopApp:run
```

`fake` 時は参加者アプリと別プロセスのため、ランキングはプロセス内メモリのみ（デモデータ + そのセッションの操作）。会場本番では `-Pquiz.runtime=prod` で参加者と同じ Firestore を参照。詳細は [docs/VERIFY.md](docs/VERIFY.md)。

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

運営 PC 向け。フォルダ（日・難易度など）ごとにクイズとランキングを管理。問題の追加・編集、解説（Markdown 風プレビュー）、「参加者向けに公開」でアクティブフォルダを切り替え。起動時にスタッフ認証画面（fake: デモ用アカウント、prod: Firebase Auth 予定）。

**開発（fake・既定）**

```bash
./gradlew :staffDesktopApp:run
```

デモ用ログイン: `staff@droidkaigi.local` / `staff2026`（インメモリのみ）。

**本番（prod）**

```bash
./gradlew :staffDesktopApp:run -Pquiz.runtime=prod
```

`core/data` の prod 実装（Firestore + `ProdStaffAuthRepository`）と `ProdStaffQuizAppGraph` が有効になります。Firebase プロジェクト・Auth の設定は上記「prod と Firestore」を参照。

## Web (Wasm)

Chrome 119+ など Wasm GC 対応ブラウザが必要です。

> **注釈（Wasm）**  
> 現在は仮で追加しており、イベント時にいろんな方が QR 経由等でアクセスする等を想定（要検討）。

```bash
./gradlew :wasmApp:wasmJsBrowserDevelopmentRun
```

## 手動確認

[docs/VERIFY.md](docs/VERIFY.md)
