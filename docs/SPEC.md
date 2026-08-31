# DroidKaigi 2026 Quiz — SPEC

## 概要

会場向け軽量クイズアプリ。選択式（単一・複数）と並び替え問題に対応し、当日ランキングを表示する。

**本番**では問題・ランキングともリモート（Firestore 等）必須。**オフライン完走は仕様に含めない**。開発時のみ `quiz.runtime=fake` で同梱 JSON とインメモリランキングを使う。

## 画面

| 画面 | 説明 |
|------|------|
| Home | ニックネーム入力、公開中フォルダが複数なら種別選択、クイズ開始。`sitePublished == false` のときは受付前メッセージを表示し開始不可。公開フォルダが 0 件のときも開始不可 |
| Quiz | 問題形式に応じた UI、進捗（1-based `n / N`）、回答。回答後は全画面フィードバックに正誤・正解・解説（登録時のみ）を表示 → ボタンで次へ／結果へ |
| Result | 正解率（0〜100%）と完全正解数。ランキングへ |
| Ranking | 当日 Top N、自分の行をハイライト、各エントリの回答完了日時（`MM/dd HH:mm`、欠落時は「不明」）を表示 |

## 問題形式 AC

- **単一選択**: 1 つ選択して提出。正解 ID と一致で正解。
- **複数選択**: 複数選択。選択集合が `correctIds` と完全一致で正解。
- **並び替え**: 右端のドラッグハンドルで順序変更。`orderedIds` が `correctOrder` と完全一致で正解。

## 採点

- `score` は 0〜100 の正解率（各問の近さの平均。時間ボーナスなし）
- 単一選択は一致で 100%、不一致で 0%
- 複数選択は Jaccard（選んだ集合と正解集合の重なり）
- 並び替えはペアの相対順が正しい割合（Kendall）。近い並びほど高い
- 結果・ランキングの主表示は `score%`。完全一致した問数は補助表示
- 同点は完了が早い順
- 回答提出〜完了判定〜採点〜ランキング送信は `QuizPlayUseCase` に集約する。ViewModel は Intent → use case → UiState/Event の変換に限定する。
- 共有プレイ状態は `QuizSessionStore`（実装: `QuizSessionHolder`）。`finishedAtEpochMillis` / `pendingResult` は ViewModel 再生成後も保持し、送信失敗時の再試行で採点結果を変えない。

## データ・ランキング

### 本番（`quiz.runtime=prod`）

- **問題**: `QuizCatalogRepository` 経由でリモート（`listPublishedFolders` → 開始時に選んだフォルダの `getQuizSet`）。
- **サイト公開**: `appConfig.sitePublished`（スタッフが ON/OFF）。参加者・スタッフは `appConfig/default` をリアルタイム購読する。Home は開きっぱなしでも受付 ON/OFF を追従し、非公開なら開始不可。プレイ中の問題セットは開始時のスナップショットのまま差し替えない。
- **ランキング**: 当日分を `dateKey` クエリでリアルタイム購読（公開フォルダが複数なら種別切替）。クイズ完了時に `SubmitScoreUseCase` で送信。回答中は開始時フォルダのスナップショットを維持し、結果ランキングもそのフォルダ（`playbackFolderId`）を表示する。
- **ネットワーク必須**。取得・送信失敗時はエラー表示（同梱 JSON やインメモリへのサイレントフォールバックなし）。

### 開発（`quiz.runtime=fake`、Gradle 既定）

- **問題**: 同梱 `quiz_set.json`（`FakeQuizCatalogSeeder` / `InMemoryQuizCatalog`、fake のみ）。
- **ランキング**: `FakeRankingRepository`（プロセス内インメモリ、端末日付でフィルタ）。
- ネットなしで UI・採点・画面遷移を検証する**開発専用ハーネス**。本番の代替実装ではない。

## 非機能

- 本番: 問題・ランキングはオンライン必須（上記）
- 開発: `fake` ランタイムのみオフライン検証可
- テーマは `core/ui/.../QuizTokens.kt` のみ編集で差し替え
- Tablet（幅 >= 600dp）では NavRail
- 参加者 UI とクイズ問題コンテンツ（問題文・選択肢／並び替え項目・正解表示・解説）は日本語 / 英語。端末ロケール追従に加え、Home で System / 日本語 / English を手動切替可能（端末内に永続化）
- 問題データは既存の日本語フィールドに任意の英語フィールドを併記する。英語が未登録の項目は日本語へフォールバックし、既存 Firestore ドキュメントとの後方互換を保つ

## モジュール構成（エントリ分離）

| モジュール | 役割 |
|-----------|------|
| `composeApp` | 共有 Compose UI・Nav3 |
| `androidApp` | Android エントリ（`MainActivity`） |
| `desktopApp` | Desktop エントリ（`main`） |
| `wasmApp` | Web エントリ（`wasmJs` / `ComposeViewport`）。`/` は参加者、`/staff` はスタッフコンソール |
| `staffComposeApp` / `staffDesktopApp` | スタッフ運営コンソール（Desktop JVM。Web は同じ Hosting の `/staff`） |

## スタッフアプリ（`staffDesktopApp` + Wasm `/staff`）

| ランタイム | 認証 | データ |
|------------|------|--------|
| **fake**（開発） | ローカル固定アカウント（`FakeStaffAuthRepository`）。入力ログインに加え「デモアカウントでログイン」ワンクリック可 | インメモリ（`InMemoryQuizCatalog`） |
| **prod**（本番） | Firebase Authentication（`ProdStaffAuthRepository`） | Firestore（[docs/FIRESTORE.md](FIRESTORE.md)） |

主な運営操作:

- **公開中フォルダ**の複数指定（`publishedFolderIds`。参加者 Home で選択）
- **サイト公開**の ON/OFF（`sitePublished`。参加者受付の可否）
- 選択フォルダの **参加者プレビュー**（スマホ枠ダイアログで Quiz→Result。日本語 / 英語を指定可。ランキング送信なし）
- 当日ランキングの **個別削除・本日分一括削除**（いずれも確認ダイアログ必須。prod では `request.auth != null` のときのみ Firestore 上で削除可）
- **アプリ更新通知**（prod・Desktop のみ）: ログイン後に `staffAppRelease/latest` を参照し、古い場合は Storage から DMG をダウンロードして手動インストール。Wasm `/staff` では DMG 更新は出さない

`quiz.runtime` は参加者アプリと共通。fake のローカル値は本番に持ち込まない。

## 将来（Phase 2 候補）

- Compose Styles API
- ランキング・運用機能の拡張（集計、管理 API など）
- iOS ターゲット
