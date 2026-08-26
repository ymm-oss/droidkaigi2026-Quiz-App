---
name: droidkaigi-quiz-review
description: >-
  DroidKaigi Quiz の PR / ブランチ変更をレビューする。
  ユーザーがレビュー依頼、droidkaigi-quiz-review、PR 前の自己レビュー、観点の調整を求めたときに使用。
  ローカルは Cursor Agent のみで完結（cursor-agent CLI 不要）。
---

# DroidKaigi Quiz — code review

Cursor Agent で PR / ブランチ変更をレビューする。観点の正本はこのスキル。

## トリガー

次のいずれかでこのスキルを適用する:

- `droidkaigi-quiz-review` / 「レビューして」 / 「PR 前に自己レビュー」
- 特定 PR・ブランチ・未コミット変更のレビュー依頼

## ローカル実行（デフォルト）

**Cursor Agent だけで完結する。** `cursor-agent` CLI は使わない。

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

1. [AGENTS.md](../../../AGENTS.md)
2. 変更ファイルに応じて `.cursor/rules/*.mdc`
3. PR 説明・Issue 文脈（あれば）

## レビュー観点（優先度順）

1. 正しさ（クラッシュ、採点・ランキング誤り、競合）
2. アーキテクチャ（逆依存、ViewModel のビジネスロジック、Repository 直叩き）
3. prod/fake（prod でのサイレント fake フォールバック）
4. セキュリティ（シークレット、認証迂回）
5. domain/scoring 変更時のテスト不足

**報告するのは重大な問題のみ**（最大 10 件）。フォーマット nit・命名の好みは省略。

### アーキテクチャ（`AGENTS.md`）

- 依存方向: `feature → core:ui, domain` · `data → domain` · `composeApp → feature`（**逆依存禁止**）
- エントリポイントは `:androidApp` / `:desktopApp` / `:wasmApp` / `:staffDesktopApp` のみ。`composeApp` に `main()` や `MainActivity` を置かない
- ナビゲーションルートは `composeApp` のみ

### MVI（`.cursor/rules/quiz-feature.mdc`）

- 画面: `XxxScreen` — stateless、`StateFlow` 収集、`Event` は `LaunchedEffect`
- ViewModel に `android.*` を import しない。`AppDependencies.shared` を使う
- Feature から `RankingRepository` を直接呼ばず use case 経由

### Domain / Data（`.cursor/rules/quiz-domain-data.mdc`）

- `core:domain` は Compose / Android に依存しない
- 採点は `QuizScorer`、セッション進行は `QuizEngine` のみ
- **prod**: リモート必須。fake / JSON へのサイレントフォールバック禁止
- **fake**: 開発・テスト用。`quiz.runtime` と Metro グラフの整合
- 「今日」のフィルタは `InstantProvider` + `isSameDay`（UI で日付ハードコードしない）

### UI / テーマ

- `QuizTheme { }` と `QuizTokens` / `QuizColors` を使用（feature で `Color(0x…)` 直書きしない）

### テスト

- 採点・日付ロジック: `commonTest`
- UI フロー: `androidInstrumentedTest`
- domain / data 変更時は `./gradlew :core:domain:jvmTest :core:data:jvmTest` の追加・更新を確認

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
- 観点の変更はこのスキルを編集する

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
