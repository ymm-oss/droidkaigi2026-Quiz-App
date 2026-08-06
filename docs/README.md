# Docs site (VitePress)

仕様書サイトのソースです。既存の `SPEC.md` / `DEVELOPMENT.md` などはリポジトリ参照用として残し、サイトページからは除外しています。

## ローカル

```bash
cd docs
npm ci
npm run docs:dev      # http://localhost:5173/droidkaigi2026-Quiz-App/
npm run docs:build    # 出力: .vitepress/dist
npm run docs:preview
```

## 公開

[`.github/workflows/docs.yml`](../.github/workflows/docs.yml) が `master` への `docs/**` 変更でビルドし、GitHub Pages にデプロイします。

公開 URL（想定）: https://ymm-oss.github.io/droidkaigi2026-Quiz-App/

初回はリポジトリ **Settings → Pages → Source = GitHub Actions** を有効化してください。

## 構成

| パス | 内容 |
|------|------|
| `user/` | 利用者向け（参加者・スタッフ） |
| `developer/` | 開発者向け |
| `en/` | 英語版 |
| `screenshots/` | 画面キャプチャ（`public/screenshots` から参照） |
| `public/` | 静的アセット（favicon、screenshots シンボリックリンク） |

## 日英の両立

サイト本文（`user/` / `developer/` / `index.md` と `en/` 配下）は **日本語と英語を同時に更新** する。手順・ルールは [`.cursor/skills/docs-bilingual-sync/SKILL.md`](../.cursor/skills/docs-bilingual-sync/SKILL.md)。

