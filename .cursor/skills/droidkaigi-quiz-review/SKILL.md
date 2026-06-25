---
name: droidkaigi-quiz-review
description: >-
  DroidKaigi Quiz の PR / ブランチ変更をレビューする。CI の Cursor Code Review と同じ観点。
  ユーザーがレビュー依頼、droidkaigi-quiz-review、PR 前の自己レビュー、観点の調整を求めたときに使用。
  ローカルは Cursor Agent のみで完結（cursor-agent CLI 不要）。
---

# DroidKaigi Quiz — code review

CI（`.github/workflows/cursor-code-review.yml`）と **同じ観点** でレビューする。観点の正本は [`.github/cursor-review-instructions.md`](../../../.github/cursor-review-instructions.md)。

## トリガー

次のいずれかでこのスキルを適用する:

- `droidkaigi-quiz-review` / 「レビューして」 / 「PR 前に自己レビュー」
- 特定 PR・ブランチ・未コミット変更のレビュー依頼

## ローカル実行（デフォルト）

**Cursor Agent だけで完結する。** `cursor-agent` CLI や `.github/scripts/` は使わない。

- ファイルの変更・コミット・`gh` による PR コメント投稿は **しない**（ユーザーが明示的に投稿を依頼した場合のみ `gh` コマンドを提案してよい）
- 結果はチャットにマークダウンで返す

## Diff の取り方

ユーザー指定がなければ **`branch changes`**（merge-base 対 base ブランチ + 未コミット分）。

| ユーザー意図 | Diff |
|--------------|------|
| 未指定・PR 前・ブランチ全体 | `branch changes`（base は `master`、なければ `main`） |
| 未コミットのみ / working tree / dirty | `uncommitted changes` |
| PR 番号・PR URL | `pr` — `gh pr diff` または `gh pr view` で文脈取得 |
| 特定 base ブランチ | `branch changes` + `Base Branch: <name>` |

### 手順

1. リポジトリルートで作業していることを確認
2. PR 指定時: 対象ブランチが checkout 済みか確認。未 checkout なら切り替え（競合時は stash はユーザー確認後）
3. Diff を取得:
   - `branch changes`: `git diff <base>...HEAD` と `git diff`（未コミット）
   - `uncommitted changes`: `git diff` と `git diff --cached`
   - `pr`: `gh pr diff <N>`、`gh pr view <N> --json title,body`
4. diff が空なら「レビュー対象の変更がありません」と返して終了

## レビュー前に読むもの

1. [`.github/cursor-review-instructions.md`](../../../.github/cursor-review-instructions.md)（必須）
2. [AGENTS.md](../../../AGENTS.md)
3. 変更ファイルに応じて `.cursor/rules/*.mdc`
4. PR 説明・Issue 文脈（あれば）

## レビュー観点（優先度順）

[`.github/cursor-review-instructions.md`](../../../.github/cursor-review-instructions.md) に委譲。要約:

1. 正しさ（クラッシュ、採点・ランキング誤り、競合）
2. アーキテクチャ（逆依存、ViewModel のビジネスロジック、Repository 直叩き）
3. prod/fake（prod でのサイレント fake フォールバック）
4. セキュリティ（シークレット、認証迂回）
5. domain/scoring 変更時のテスト不足

**報告するのは重大な問題のみ**（最大 10 件）。フォーマット nit・命名の好みは省略。

## 出力フォーマット

必ずこの形で返す（日本語）:

```markdown
## Cursor レビュー

**対象:** <branch名 / uncommitted / PR #N>
**重大な問題:** <件数>

| 重要度 | 場所 | 内容 |
|--------|------|------|
| 🚨 / ⚠️ / 🔒 | path:line | 1〜2 文 |

### サマリー
<マージブロッカーの有無、推奨テスト、2〜4 文>
```

- 🚨 本番クラッシュ・採点ミス等
- ⚠️ バグの可能性・アーキテクチャ違反
- 🔒 セキュリティ

問題がなければ件数 `0` と「重大な問題は見つかりませんでした」を明記。

## レビュー後

- **修正はしない**（ユーザーが依頼した場合のみ）
- 再レビューはユーザーが依頼するまで行わない
- 観点の変更は [`.github/cursor-review-instructions.md`](../../../.github/cursor-review-instructions.md) を編集（ローカル・CI 共通の正本）

## CI との関係

| 環境 | 実行主体 | 投稿 |
|------|----------|------|
| ローカル（このスキル） | Cursor Agent | チャットのみ |
| GitHub Actions | `cursor-agent` CLI | `gh` で PR コメント |

**推奨フロー:** このスキルでローカル確認 → 観点 OK ならテスト PR で CI 確認。

CI セットアップ: `CURSOR_API_KEY` をリポジトリ Secret に登録し、非ドラフト PR で [`.github/workflows/cursor-code-review.yml`](../../../.github/workflows/cursor-code-review.yml) を確認。

## ユーザー向けの依頼例

```
droidkaigi-quiz-review で branch changes をレビューして
```

```
未コミット変更だけレビューして
```

```
PR #42 をレビューして（投稿はしない）
```
