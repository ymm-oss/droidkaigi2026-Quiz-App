---
name: docs-bilingual-sync
description: >-
  Keeps DroidKaigi Quiz VitePress docs (JA root + EN under docs/en/) in sync when
  adding or editing specification pages. Use when updating docs/user, docs/developer,
  docs/en/**, docs/index.md, sidebar config in docs/.vitepress, or when the user asks
  for 日英ドキュメント同期, bilingual docs, VitePress i18n, or docs site content changes.
---

# Docs 日英同期（VitePress）

仕様書サイト（`docs/` + VitePress）は **日本語（root）と英語（`docs/en/`）を常に両立** させる。  
片方だけの追加・更新は禁止。編集したら必ずペアを揃えてから完了とする。

個人スキル `readme-bilingual-sync`（README ペア専用）とは別。本スキルは **VitePress 仕様書** 向け。

## いつ適用するか

- `docs/user/**` / `docs/developer/**` / `docs/index.md` を追加・変更するとき
- `docs/en/**` を追加・変更するとき
- `docs/.vitepress/ja.ts` / `en.ts` の nav・sidebar を触るとき
- 「ドキュメント更新」「仕様書に追記」「日英を揃えて」などの依頼

**対象外（このスキルの必須同期外）**

| パス | 理由 |
|------|------|
| `docs/SPEC.md` などレガシー Markdown | サイトから `srcExclude`。必要なら別途相談 |
| `docs/screenshots/**` | 画像は言語共通。キャプション文言だけ各言語ページで揃える |
| `docs/package.json` / lock / CI workflow | コード・設定。本文の日英ペアではない |

## ペア対応表

| 日本語（正のパス例） | 英語 |
|----------------------|------|
| `docs/index.md` | `docs/en/index.md` |
| `docs/user/...` | `docs/en/user/...` |
| `docs/developer/...` | `docs/en/developer/...` |
| `docs/.vitepress/ja.ts`（nav/sidebar） | `docs/.vitepress/en.ts` |

リンクは **同一ロケール内** を指す（JA ページから `/en/...` へ飛ばさない。言語スイッチャーに任せる）。  
例外: 意図的な「英語版はこちら」誘導のみ。

## ルール

1. **同時更新**: 新規ページは JA + EN を同 PR / 同作業で追加。sidebar も両ロケールに同じ項目を足す。
2. **構成一致**: 見出しレベル・セクション順・箇条書き数・表の行列を揃える。情報量を片方で落とさない。
3. **意味の同等**: 機械翻訳の貼り付け禁止。既存ページの用語・敬体（JA ですます / EN 既存トーン）に合わせる。
4. **コード・コマンド・パス**: 原則そのまま。説明文とキャプションだけ言語化。
5. **技術固有名**: Kotlin / Gradle / Firestore / `quiz.runtime` などは原文維持。
6. **スクショ**: `src="/screenshots/..."` は両言語で同一パス。`alt` / 前後の説明文は言語別。
7. **Front matter**（`layout: home` の hero 等）: キー構造を揃え、表示文言だけ翻訳。
8. **不確実な仕様**: 推測で埋めず、ユーザーに確認するか「既存 SPEC に合わせる」と明記。

## 手順

### A. 既存ページの更新

1. 編集対象の **対になるパス** を特定する（上表）。
2. どちらをソースにするか:
   - ユーザー指定があれば従う
   - なければ今回の差分・依頼内容がある側をソースにする
3. ソースの変更点をセクション単位で列挙し、ターゲットへ反映する。
4. ページ内リンク・画像・表を両ファイルで突き合わせる。
5. sidebar（`ja.ts` / `en.ts`）に項目追加が必要なら **両方** 更新。

### B. 新規ページの追加

1. JA パスに本文を書く（または先に EN。どちらでもよいが **片方で止めない**）。
2. 同じ相対パスで `docs/en/` 配下に対応ページを作る。
3. `docs/.vitepress/ja.ts` と `en.ts` の sidebar / nav に同じ位置でリンクを追加する。
4. 他ページからの相互リンクがあれば、JA→JA・EN→EN の両方を直す。

### C. 完了前チェック

```text
- [ ] 変更した JA ページすべてに EN ペアがある（またはその逆）
- [ ] 見出し構成・リスト件数・表が一致
- [ ] ja.ts / en.ts の sidebar 項目が対応
- [ ] ロケール内リンクが壊れていない
- [ ] （可能なら）cd docs && npm run docs:build
```

### D. 報告

- 更新した JA / EN パス一覧
- sidebar 変更の有無
- 意図的に同期しなかったもの（あれば理由）

## 役立つヒント

### 用語の揃え方

既存の対訳を優先する。迷ったら同じセクションの近傍ページを開いて表記をコピーする。

| 概念 | JA | EN |
|------|----|----|
| 参加者アプリ | 参加者アプリ | Participant app |
| 管理者 / スタッフ | 管理者アプリ / スタッフ | Staff app |
| 公開中フォルダ | 公開中フォルダ | Published / active folder |
| 単一選択 / 複数選択 / 並び替え | 同上 | Single choice / Multiple choice / Reorder |
| ランタイム | fake / prod | fake / prod（訳さない） |

### 差分の見つけ方

```bash
# ペアの有無（例: user 配下）
comm -3 \
  <(cd docs/user && find . -name '*.md' | sort) \
  <(cd docs/en/user && find . -name '*.md' | sort)

# 直近で触ったファイル
git log -1 --format='%h %ci %s' -- docs/user/participant/home.md
git log -1 --format='%h %ci %s' -- docs/en/user/participant/home.md
```

見出しだけ素早く比較するなら、両ファイルの `^#+ ` 行を並べて見る。

### ローカル確認

```bash
cd docs && npm ci && npm run docs:dev
# JA: /droidkaigi2026-Quiz-App/
# EN: /droidkaigi2026-Quiz-App/en/
```

ビルドだけなら `npm run docs:build`。詳細は [docs/README.md](../../../docs/README.md)。

### よくある失敗

- JA だけ sidebar に追加して EN にページがない → 英語 UI で 404
- EN リンクを `/user/...` のままにして root（JA）に飛ぶ → `/en/user/...` にする
- レガシー `SPEC.md` だけ更新してサイトページを忘れ → 利用者向けサイトは `docs/user` / `docs/developer`（と `en`）が正
- スクショの `alt` だけ英語のまま → アクセシビリティ的に言語を揃える

## 関連

- サイト構成・公開: [docs/README.md](../../../docs/README.md)
- README 日英（別用途）: 個人スキル `readme-bilingual-sync`
- エージェント索引: [AGENTS.md](../../../AGENTS.md)
