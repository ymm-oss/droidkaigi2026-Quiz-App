# コントリビューションガイド

DroidKaigi 2026 Quiz への参加方法です。環境構築・ビルド手順は [DEVELOPMENT.md](DEVELOPMENT.md) を参照してください。

## はじめに

**誰でも PR を歓迎します。** 小さな修正、ドキュメント改善、不具合報告も問題ありません。わからない方・質問は気軽に Issue や Slack で。

## 相談・質問

| 手段 | 向いていること |
|------|----------------|
| [Slack `#191_eve_droidkaigi`](https://accenture.enterprise.slack.com/archives/C0AMEMFCL4T) | 手早い質問、実装中の相談、会場運営まわりの確認 |
| [GitHub Issue](https://github.com/ymm-oss/droidkaigi2026-Quiz-App/issues) | 設計の議論、バグ・機能のトラッキング、後から参照したい記録 |

Slack は **Accenture Enterprise Slack** のワークスペース参加が必要です（招待はチーム経由）。

## 不具合・新機能の提案

- **不具合** — Issue を作成するか、再現手順付きで PR を出す
- **新機能・改善** — 大きめの変更は先に Issue で方針を相談。小さければ PR 直接でも OK

## レビューとマージ

- PR は **Approve が 1 件** あればマージできます
- レビューコメントへの対応後、再度レビューを依頼してください

## 作業の流れ

1. Issue または Slack で相談（必要に応じて）
2. `master` から feature ブランチを切る
3. 実装（[DEVELOPMENT.md](DEVELOPMENT.md) の fake ランタイムでオフライン検証可能）
4. テスト・手動確認
5. PR を作成

## PR チェックリスト

マージ前に以下を確認してください。

- [ ] [docs/SPEC.md](SPEC.md) の該当 AC を満たしている
- [ ] ユニットテスト: `./gradlew :core:domain:jvmTest :core:data:jvmTest`
- [ ] UI 変更時は `./gradlew :androidApp:connectedDebugAndroidTest`（エミュレータ要）または [docs/VERIFY.md](VERIFY.md) で手動確認
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
| [.cursor/skills/droidkaigi-quiz-review/](../.cursor/skills/droidkaigi-quiz-review/) | PR レビュー（ローカルは Agent + スキル、CI と同観点） |

### おすすめの進め方

1. **Plan モード** — 仕様・影響範囲を整理してから実装に入る
2. **Agent モード** — `docs/SPEC.md` と `AGENTS.md` を前提に実装に着手
3. **PR 前** — `jvmTest` と必要に応じて `connectedDebugAndroidTest` / [VERIFY.md](VERIFY.md) を実行

### CI 自動レビュー（Cursor CLI）

PR 作成・更新時に [`.github/workflows/cursor-code-review.yml`](../.github/workflows/cursor-code-review.yml) が **Cursor CLI** で diff をレビューし、GitHub にコメントします（ドラフト PR は対象外）。

**初回セットアップ（リポジトリ管理者）**

1. [Cursor Dashboard → Integrations](https://cursor.com/dashboard/integrations) で API キーを発行
2. GitHub リポジトリの **Settings → Secrets and variables → Actions** に `CURSOR_API_KEY` を登録

```bash
gh secret set CURSOR_API_KEY --repo OWNER/REPO --body "$CURSOR_API_KEY"
```

レビュー観点は [`.github/cursor-review-instructions.md`](../.github/cursor-review-instructions.md) と `.cursor/rules/` に準拠します。CLI の権限は [`.cursor/cli.json`](../.cursor/cli.json) で読み取り + `gh` コメントのみに制限しています。

**動作確認の推奨順序**

1. **ローカル（Cursor Agent + スキル）** — CLI 不要。Agent に例えば「`droidkaigi-quiz-review` で branch changes をレビューして」と依頼。詳細は [`.cursor/skills/droidkaigi-quiz-review/`](../.cursor/skills/droidkaigi-quiz-review/SKILL.md)。
2. **テスト PR + CI** — `CURSOR_API_KEY` 登録後、非ドラフト PR を作成し Actions ログと PR コメントを確認。

## コーディング規約

詳細は [AGENTS.md](../AGENTS.md) と [.cursor/rules/](../.cursor/rules/) に委譲します。要点のみ:

- モジュール依存は `feature → core` のみ（逆依存禁止）
- 画面は MVI（`XxxUiState` / `XxxIntent` / `XxxEvent` / `XxxViewModel`）
- ビジネスロジックは domain の use case へ
- テーマは `QuizTheme` / `QuizTokens` を使用
