# Firestore — データベース構造と prod 実装

`quiz.runtime=prod` 時のバックエンド仕様。prod ビルドの準備は [DEVELOPMENT.md#firebase-セットアップ](DEVELOPMENT.md#firebase-セットアップ) を参照。

## コレクション構成

ドメイン（`QuizFolder` / `QuizSet` / `Question`）および同梱 [quiz_set.json](../core/data/src/commonMain/composeResources/files/quiz_set.json) に合わせ、**フォルダ ID = クイズセット ID** の 1:1 を維持する。

```
folders/{folderId}
  name: string
  description: string
  sortOrder: number
  title: string                 # QuizSet.title
  questions: array<map>         # 出題順。QuestionDto と同型
    type: "single_choice" | "multiple_choice" | "reorder"
    id, prompt, explanationMarkdown?
    options? / correctId? / correctIds? / items? / correctOrder?
  updatedAtEpochMillis: number?  # 任意

appConfig/default              # ドキュメント ID 固定
  activeFolderId: string        # 参加者向けに公開中のフォルダ
  sitePublished: boolean        # サイト／受付の公開可否（既定 false）
  updatedAtEpochMillis: number?

staffAppRelease/latest         # ドキュメント ID 固定。スタッフ Desktop 最新版メタ（認証必須）
  version: string               # SemVer 例 "1.2.0"
  versionCode: number           # 比較用（major*10000+minor*100+patch）
  storagePath: string           # Storage オブジェクトパス（公開 URL ではない）
  sha256: string                # DMG の SHA-256（hex）
  releaseNotes: string
  publishedAtEpochMillis: number?

folders/{folderId}/rankings/{entryId}
  nickname: string
  score: number
  completedAtEpochMillis: number
  dateKey: string               # 例 "2026-06-04"（InstantProvider の当日）
```

### Firebase Storage（スタッフ DMG）

```
releases/staff-desktop/{version}.dmg
```

読み取りは認証済みスタッフのみ（[`storage.rules`](../storage.rules)）。書き込みは Admin SDK / Release CD のサービスアカウント。

### 設計の意図

| 観点 | 説明 |
|------|------|
| 読み取り回数 | 参加者起動時は `getActiveFolderId` → `getQuizSet` の **2 読み取り**で足りる |
| シード | fake は同梱 `quiz_set.json`。Firestore 上の `questions` は同型（参考: [firestore-seed.json](firestore-seed.json)） |
| ドキュメントサイズ | 会場想定の問題数なら 1 フォルダ 1 ドキュメントで 1 MiB 以内 |
| ランキング | サブコレクションに分離し、提出増加でフォルダ本体が肥大化しない |

## インデックス

当日ランキング取得用の **複合インデックス**。定義: [firestore.indexes.json](../firestore.indexes.json)

| コレクション | フィールド |
|--------------|------------|
| `folders/{folderId}/rankings` | `dateKey` 昇順、`score` 降順 |

### クエリとフォールバック（`GitLiveFirestoreService.listRankingsForDate`）

1. **通常**: `dateKey == target` + `orderBy(score DESC)`（上記複合インデックスが必要）
2. **フォールバック**（複合インデックス不足時のみ）: `dateKey == target` の等値クエリ → クライアント側で `score` 降順ソート  
   - 判定: GitLive の `FirebaseFirestoreException.code == FAILED_PRECONDITION`（型優先）。型が取れない場合のみメッセージの `FAILED_PRECONDITION` / `requires an index` を見る
   - 通信障害・権限エラーなど **それ以外の例外は握りつぶさず上位へ伝播**（参加者 UI でエラー表示）
   - **フォルダ内ランキングの全件取得フォールバックはしない**（過去日分の読み取り肥大化を避ける）

### インデックスのデプロイ

未デプロイだと通常クエリが `FAILED_PRECONDITION` になり、上記等値フォールバックに落ちる。本番・結合前にデプロイする:

```bash
firebase deploy --only firestore:indexes
```

手順の位置づけは [DEVELOPMENT.md#firestore-インデックスのデプロイ](DEVELOPMENT.md#firestore-インデックスのデプロイ) も参照。

## セキュリティルール

会場公開クイズ + 匿名スコア送信 + ログイン済みスタッフのフォルダ編集向け。

全文: [firestore.rules](../firestore.rules)

要点:

- `folders` / `appConfig`: 読み取り全員、書き込み `request.auth != null`（スタッフ）
- `rankings`: 読み取り全員、`create`（参加者のスコア送信）、`delete` はログイン済みスタッフのみ、`update` 不可
- `staffAppRelease`: 読み取り `request.auth != null`、クライアント書き込み不可（CD / Admin SDK）
- Storage `releases/staff-desktop/**`: 読み取り `request.auth != null`、クライアント書き込み不可

## アプリからのマッピング

| Repository | Firestore / Storage |
|------------|---------------------|
| `RemoteQuizCatalogRepository` | `folders`, `appConfig/default`（`activeFolderId` / `sitePublished`） |
| `RemoteRankingRepository` | `folders/{id}/rankings` |
| `RemoteStaffAppReleaseRepository` | `staffAppRelease/latest` + Storage `releases/staff-desktop/{version}.dmg` |
| 参加者クイズ取得 | `getActiveQuizFolderIdUseCase` → `getQuizSetForFolderUseCase`（`folders/{activeFolderId}`） |
| サイト公開 | `getSitePublishedUseCase` / `setSitePublishedUseCase`（`appConfig/default.sitePublished`） |
| スタッフ Desktop 更新 | `checkForStaffAppUpdateUseCase` / `downloadStaffAppUpdateUseCase` |

**prod のデータ取得**

- `QuizRepository` / `getDefaultQuizSet` は使わない。参加者・スタッフとも `QuizCatalogRepository` 経由。
- `RemoteRankingRepository` は `folders/{folderId}/rankings` を `dateKey` + `score` でクエリし、`InstantProvider` の「当日」と揃える（インデックス不足時の挙動は [クエリとフォールバック](#クエリとフォールバックgitlivefirestoreservicelistrankingsfordate)）。

`firestore.rules` / `storage.rules` の本番反映は `master` マージ時の CD（[DEVELOPMENT.md#cdmaster-マージ時のルール自動デプロイ](DEVELOPMENT.md#cdmaster-マージ時のルール自動デプロイ)）を使う。

### prod 実装クラス（`core:data`）

| クラス | プラットフォーム | 役割 |
|--------|------------------|------|
| `GitLiveFirestoreService` | Android / Desktop JVM | GitLive Firestore SDK（`prodGitLive`） |
| `FirestorePlatform.jvm` | Desktop JVM | `firebase-java-sdk` で `Firebase.initialize` |
| `ProdStaffAuthRepository` | Android / JVM | Firebase Auth（メール・パスワード） |

`updatedAtEpochMillis` は未設定可。

## ランタイムとアプリの対応

| アプリ | fake | prod |
|--------|------|------|
| 参加者 Android / Desktop | 同梱 JSON + ローカルランキング | Firestore |
| 参加者 Wasm | 同左 | **未対応**（起動時エラー） |
| スタッフ Desktop | インメモリ + デモログイン | Firebase Auth + Firestore |

スタッフも `quiz.runtime` で fake/prod が切り替わる（`staffComposeApp` の Metro グラフと `core:data` が連動）。
