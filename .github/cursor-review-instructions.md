# Cursor 自動 PR レビュー — リポジトリ固有の観点

このファイルは `.github/workflows/cursor-code-review.yml` から参照されるレビュー観点です。

## アーキテクチャ（`AGENTS.md`）

- 依存方向: `feature → core:ui, domain` · `data → domain` · `composeApp → feature`（**逆依存禁止**）
- エントリポイントは `:androidApp` / `:desktopApp` / `:wasmApp` / `:staffDesktopApp` のみ。`composeApp` に `main()` や `MainActivity` を置かない
- ナビゲーションルートは `composeApp` のみ

## MVI（`.cursor/rules/quiz-feature.mdc`）

- 画面: `XxxScreen` — stateless、`StateFlow` 収集、`Event` は `LaunchedEffect`
- ViewModel に `android.*` を import しない。`AppDependencies.shared` を使う
- Feature から `RankingRepository` を直接呼ばず use case 経由

## Domain / Data（`.cursor/rules/quiz-domain-data.mdc`）

- `core:domain` は Compose / Android に依存しない
- 採点は `QuizScorer`、セッション進行は `QuizEngine` のみ
- **prod**: リモート必須。fake / JSON へのサイレントフォールバック禁止
- **fake**: 開発・テスト用。`quiz.runtime` と Metro グラフの整合
- 「今日」のフィルタは `InstantProvider` + `isSameDay`（UI で日付ハードコードしない）

## UI / テーマ

- `QuizTheme { }` と `QuizTokens` / `QuizColors` を使用（feature で `Color(0x…)` 直書きしない）

## テスト

- 採点・日付ロジック: `commonTest`
- UI フロー: `androidInstrumentedTest`
- domain / data 変更時は `./gradlew :core:domain:jvmTest :core:data:jvmTest` の追加・更新を確認

## コメント方針

- 本番クラッシュ・採点ミス・ランキング不整合・モジュール境界違反を最優先
- 1 コメント 1〜2 文、日本語、インラインは最大 10 件
- スタイル指摘や好みの議論は省略
