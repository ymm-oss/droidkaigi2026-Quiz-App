# DroidKaigi 2026 Quiz — SPEC

## 概要

会場向け軽量クイズアプリ。選択式（単一・複数）と並び替え問題に対応し、当日ランキングを表示する。

**本番**では問題・ランキングともリモート（Firestore 等）必須。**オフライン完走は仕様に含めない**。開発時のみ `quiz.runtime=fake` で同梱 JSON とインメモリランキングを使う。

## 画面

| 画面 | 説明 |
|------|------|
| Home | ニックネーム入力、クイズ開始 |
| Quiz | 問題形式に応じた UI、進捗、回答 |
| Result | スコア表示（アニメーション）、ランキングへ |
| Ranking | 当日 Top N、自分の行をハイライト |

## 問題形式 AC

- **単一選択**: 1 つ選択して提出。正解 ID と一致で正解。
- **複数選択**: 複数選択。選択集合が `correctIds` と完全一致で正解。
- **並び替え**: 右端のドラッグハンドルで順序変更。`orderedIds` が `correctOrder` と完全一致で正解。

## 採点

- `score = correctCount * 100 + timeBonus`
- `timeBonus = (50 - elapsedSeconds).coerceIn(0, 50)`

## データ・ランキング

### 本番（`quiz.runtime=prod`）

- **問題**: `QuizCatalogRepository` 経由でリモート（`getActiveFolderId` → `getQuizSet`）。
- **ランキング**: リモートから当日分を取得。クイズ完了時に `SubmitScoreUseCase` で送信。
- **ネットワーク必須**。取得・送信失敗時はエラー表示（同梱 JSON やインメモリへのサイレントフォールバックなし）。

### 開発（`quiz.runtime=fake`、Gradle 既定）

- **問題**: 同梱 `quiz_set.json`（`JsonQuizRepository` / `InMemoryQuizCatalog`）。
- **ランキング**: `FakeRankingRepository`（プロセス内インメモリ、端末日付でフィルタ）。
- ネットなしで UI・採点・画面遷移を検証する**開発専用ハーネス**。本番の代替実装ではない。

## 非機能

- 本番: 問題・ランキングはオンライン必須（上記）
- 開発: `fake` ランタイムのみオフライン検証可
- テーマは `core/ui/.../QuizTokens.kt` のみ編集で差し替え
- Tablet（幅 >= 600dp）では NavRail

## モジュール構成（エントリ分離）

| モジュール | 役割 |
|-----------|------|
| `composeApp` | 共有 Compose UI・Nav3 |
| `androidApp` | Android エントリ（`MainActivity`） |
| `desktopApp` | Desktop エントリ（`main`） |
| `wasmApp` | Web エントリ（`wasmJs` / `ComposeViewport`） |
| `staffComposeApp` / `staffDesktopApp` | スタッフ運営コンソール（Desktop） |

## スタッフアプリ（`staffDesktopApp`）

| ランタイム | 認証 | データ |
|------------|------|--------|
| **fake**（開発） | ローカル固定アカウント（`FakeStaffAuthRepository`） | インメモリ（`InMemoryQuizCatalog`） |
| **prod**（本番） | Firebase Authentication（`ProdStaffAuthRepository`・実装予定） | Firestore（参加者 prod と同系統・スタッフは書き込み可） |

`quiz.runtime` は参加者アプリと共通。fake のローカル値は本番に持ち込まない。

## 将来（Phase 2 候補）

- Compose Styles API
- ランキング・運用機能の拡張（集計、管理 API など）
- iOS ターゲット
- `ProdStaffAuthRepository` と Firestore スタッフ書き込みの本実装
