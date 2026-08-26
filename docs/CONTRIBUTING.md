# コントリビューションガイド

DroidKaigi 2026 Quiz への参加方法です。環境構築・ビルド手順は [DEVELOPMENT.md](DEVELOPMENT.md) を参照してください。

## はじめに

**誰でも PR を歓迎します。** 小さな修正、ドキュメント改善、不具合報告も問題ありません。わからない方・質問は気軽に [GitHub Issue](https://github.com/ymm-oss/droidkaigi2026-Quiz-App/issues) で。

## 相談・質問

| 手段 | 向いていること |
|------|----------------|
| [GitHub Issue](https://github.com/ymm-oss/droidkaigi2026-Quiz-App/issues) | 手早い質問、設計の議論、バグ・機能のトラッキング、後から参照したい記録 |

## 不具合・新機能の提案

- **不具合** — Issue を作成するか、再現手順付きで PR を出す
- **新機能・改善** — 大きめの変更は先に Issue で方針を相談。小さければ PR 直接でも OK

## レビューとマージ

- PR は **Approve が 1 件** あればマージできます
- レビューコメントへの対応後、再度レビューを依頼してください

## 作業の流れ

1. Issue で相談（必要に応じて）
2. `master` から feature ブランチを切る
3. 実装（[DEVELOPMENT.md](DEVELOPMENT.md) の fake ランタイムでオフライン検証可能）
4. テスト・手動確認
5. PR を作成

## PR チェックリスト

マージ前に以下を確認してください。

- [ ] [docs/SPEC.md](SPEC.md) の該当 AC を満たしている
- [ ] ユニットテスト: `./gradlew :core:domain:jvmTest :core:data:jvmTest`
- [ ] UI 変更時は `./gradlew :androidApp:connectedDebugAndroidTest`（エミュレータ要）または [docs/VERIFY.md](VERIFY.md) で手動確認
- [ ] 管理者アプリ UI 変更時は PR 本文にスクショを埋め込み（比較表・縮小。[staff-pr-screenshots](../.cursor/skills/staff-pr-screenshots/SKILL.md)）
- [ ] `quiz.runtime` や Firestore 周りを触った場合は prod ビルド・結合確認の手順を PR 説明に記載

## AI 駆動開発（推奨）

このリポジトリは **Cursor などの AI エディタでの開発を推奨** しています。ルールとスキルが整備済みなので、エージェントにコンテキストを渡しやすい構成です。

### リポジトリ内の AI 向け資産

| パス | 用途 |
|------|------|
| [AGENTS.md](../AGENTS.md) | モジュール境界・ドキュメント索引 |
| [.cursor/rules/](../.cursor/rules/) | 常時 / ファイル別ルール（MVI、Metro、テーマなど） |
| [.cursor/skills/droidkaigi-quiz/](../.cursor/skills/droidkaigi-quiz/) | 実装ワークフロー（SPEC → CHECKLIST → domain → data → feature → nav → test） |
| [.cursor/skills/droidkaigi-quiz-test/](../.cursor/skills/droidkaigi-quiz-test/) | テスト追加手順 |
| [.cursor/skills/droidkaigi-quiz-verify/](../.cursor/skills/droidkaigi-quiz-verify/) | 手動確認手順 |
| [.cursor/skills/droidkaigi-quiz-review/](../.cursor/skills/droidkaigi-quiz-review/) | PR レビュー（Cursor Agent + スキル） |
| [.cursor/skills/jvm-compose-screenshot/](../.cursor/skills/jvm-compose-screenshot/) | JVM Compose スクショ取得 |
| [.cursor/skills/staff-pr-screenshots/](../.cursor/skills/staff-pr-screenshots/) | 管理者アプリ UI の PR 埋め込み（必須） |

### おすすめの進め方

1. **Plan モード** — 仕様・影響範囲を整理してから実装に入る
2. **Agent モード** — `docs/SPEC.md` と `AGENTS.md` を前提に実装に着手
3. **PR 前** — `jvmTest` と必要に応じて `connectedDebugAndroidTest` / [VERIFY.md](VERIFY.md) を実行。任意で Agent に「`droidkaigi-quiz-review` で branch changes をレビューして」と依頼（詳細は [`.cursor/skills/droidkaigi-quiz-review/`](../.cursor/skills/droidkaigi-quiz-review/SKILL.md)）

## コーディング規約

詳細は [AGENTS.md](../AGENTS.md) と [.cursor/rules/](../.cursor/rules/) に委譲します。要点のみ:

- モジュール依存は `feature → core` のみ（逆依存禁止）
- 画面は MVI（`XxxUiState` / `XxxIntent` / `XxxEvent` / `XxxViewModel`）
- ビジネスロジックは domain の use case へ
- テーマは `QuizTheme` / `QuizTokens` を使用
