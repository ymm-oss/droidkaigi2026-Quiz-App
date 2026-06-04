# DroidKaigi 2026 Quiz — SPEC

## 概要

会場向け軽量クイズアプリ。選択式（単一・複数）と並び替え問題に対応し、当日のモックランキングを表示する。

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

## ランキング

- `FakeRankingRepository` が当日（端末日付）のエントリをスコア降順で返す。
- クイズ完了時に `SubmitScoreUseCase` 経由で登録。

## 非機能

- オフライン（JSON + ローカルランキング）
- テーマは `core/ui/.../QuizTokens.kt` のみ編集で差し替え
- Tablet（幅 >= 600dp）では NavRail

## モジュール構成（エントリ分離）

| モジュール | 役割 |
|-----------|------|
| `composeApp` | 共有 Compose UI・Nav3 |
| `androidApp` | Android エントリ（`MainActivity`） |
| `desktopApp` | Desktop エントリ（`main`） |
| `wasmApp` | Web エントリ（`wasmJs` / `ComposeViewport`） |

## 将来（Phase 2 候補）

- Compose Styles API
- リモートランキング API
- iOS ターゲット
