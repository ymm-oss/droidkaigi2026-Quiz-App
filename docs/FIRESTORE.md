# Firestore — データベース構造と prod 実装

`quiz.runtime=prod` 時のバックエンド仕様。Firebase プロジェクトの初回セットアップは [DEVELOPMENT.md#firebase-セットアップ](DEVELOPMENT.md#firebase-セットアップ) を先に完了すること。

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
  updatedAtEpochMillis: number?

folders/{folderId}/rankings/{entryId}
  nickname: string
  score: number
  completedAtEpochMillis: number
  dateKey: string               # 例 "2026-06-04"（InstantProvider の当日）
```

### 設計の意図

| 観点 | 説明 |
|------|------|
| 読み取り回数 | 参加者起動時は `getActiveFolderId` → `getQuizSet` の **2 読み取り**で足りる |
| シード | `questions` は `QuizSetDto` / `quiz_set.json` と同型で Console 投入が容易 |
| ドキュメントサイズ | 会場想定の問題数なら 1 フォルダ 1 ドキュメントで 1 MiB 以内 |
| ランキング | サブコレクションに分離し、提出増加でフォルダ本体が肥大化しない |

### 避ける形

- 全問題を `quizSets/global` 1 件に集約 → `QuizCatalogRepository` のフォルダ API とずれる
- ランキングをフォルダドキュメント内配列のみ → 当日の提出で競合・サイズ増大

### 将来の拡張

問題単位の差分更新・同時編集が必要になったら `folders/{folderId}/questions/{questionId}` サブコレクション + `sortOrder` へ分割。初版は `questions` 配列のままでよい。

## 初期データ

[firestore-seed.json](firestore-seed.json) にデモ用フォルダ `droidkaigi2026-demo` と `appConfig/default` のサンプルがある。投入方法は次のいずれか。

### A. Firebase Console で手動投入（推奨・初回）

1. [Firebase Console](https://console.firebase.google.com/) → Firestore → **データ** を開く
2. コレクション `folders` → ドキュメント ID `droidkaigi2026-demo` を作成し、[firestore-seed.json](firestore-seed.json) の `folders.droidkaigi2026-demo` のフィールドを入力
3. コレクション `appConfig` → ドキュメント ID `default` を作成し、`activeFolderId: "droidkaigi2026-demo"` を設定

### B. スタッフアプリから作成

[Firebase セットアップ](DEVELOPMENT.md#firebase-セットアップ) とルールデプロイ後、`staffDesktopApp`（prod）でログインし、UI からフォルダ・問題を登録する。`appConfig/default` の `activeFolderId` は Console またはスタッフアプリの公開切替で設定する。

### 確認

参加者アプリ（prod）起動時にデモ問題が表示され、スコア送信後にランキングに反映されれば OK（[VERIFY.md](VERIFY.md)）。

## インデックス

当日ランキング取得用の **複合インデックス**（クエリ失敗時はクライアント側で `dateKey` フィルタにフォールバック）。

| コレクション | フィールド |
|--------------|------------|
| `folders/{folderId}/rankings` | `dateKey` 昇順、`score` 降順 |

## Firebase CLI でデプロイ

前提: [DEVELOPMENT.md#firebase-セットアップ](DEVELOPMENT.md#firebase-セットアップ) の CLI ログインと `.firebaserc` のプロジェクト紐づけ。

### ルールとインデックス

```bash
# リポジトリルートで実行
firebase deploy --only firestore:rules,firestore:indexes
```

| ファイル | 内容 |
|----------|------|
| [firestore.rules](../firestore.rules) | セキュリティルール |
| [firestore.indexes.json](../firestore.indexes.json) | `rankings` の `dateKey` + `score` 複合インデックス |

デプロイ後、Console の Firestore → **ルール** / **インデックス** タブで反映を確認する。インデックス構築には数分かかることがある。

### Hosting / Functions

- **Hosting**: Wasm 向け設定は [firebase.json](../firebase.json) にあるが **未デプロイ**（手順は [DEVELOPMENT.md#7-wasm-hosting将来用任意](DEVELOPMENT.md#7-wasm-hosting将来用任意)）
- **Functions**: [functions/](../functions/) は雛形のみで **`firebase.json` 未登録**。デプロイ不要

## セキュリティルール

会場公開クイズ + 匿名スコア送信 + ログイン済みスタッフのフォルダ編集向け。

全文: [firestore.rules](../firestore.rules)

要点:

- `folders` / `appConfig`: 読み取り全員、書き込み `request.auth != null`（スタッフ）
- `rankings`: 読み取り全員、`create` のみ（参加者のスコア送信）、`update`/`delete` 不可

## アプリからのマッピング

| Repository | Firestore |
|------------|-----------|
| `RemoteQuizCatalogRepository` | `folders`, `appConfig/default` |
| `RemoteRankingRepository` | `folders/{id}/rankings` |
| 参加者クイズ取得 | `getActiveQuizFolderIdUseCase` → `getQuizSetForFolderUseCase`（`folders/{activeFolderId}`） |

**prod のデータ取得**

- `QuizRepository` / `getDefaultQuizSet` は使わない。参加者・スタッフとも `QuizCatalogRepository` 経由。
- `RemoteRankingRepository` は `folders/{folderId}/rankings` を `dateKey` + `score` でクエリし、`InstantProvider` の「当日」と揃える。

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
