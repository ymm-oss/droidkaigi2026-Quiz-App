# 開発者向け概要

このセクションは **実装・運用・コントリビュート** 向けです。利用者向けの操作説明は [利用者向け概要](/user/overview) を参照してください。

## ドキュメントマップ

| ページ | 内容 |
|--------|------|
| [アーキテクチャ](/developer/architecture) | モジュール構成・依存方向・MVI |
| [採点ロジック](/developer/scoring) | スコア式・経過時間の定義 |
| [ランタイム](/developer/runtime) | `quiz.runtime=fake` / `prod` |
| [Firestore](/developer/firestore) | 本番 DB 構造・ルール |
| [ビルド・実行](/developer/build) | Gradle・Variant・スタッフ Desktop |
| [コントリビュート](/developer/contributing) | PR・レビュー・AI 開発 |

## リポジトリ内の既存 Markdown

リポジトリ直下の詳細ドキュメントも維持しています（サイトのページとしては除外）。

| ファイル | 内容 |
|----------|------|
| `docs/SPEC.md` | 画面・問題形式・採点の受け入れ基準 |
| `docs/DEVELOPMENT.md` | 環境構築・fake/prod・Firebase |
| `docs/FIRESTORE.md` | DB 構造 |
| `docs/CONTRIBUTING.md` | 参加方法 |
| `docs/VERIFY.md` | 手動確認 |
| `AGENTS.md` | モジュール境界・AI 向け索引 |

## クイックリンク

- GitHub: [ymm-oss/droidkaigi2026-Quiz-App](https://github.com/ymm-oss/droidkaigi2026-Quiz-App)
- パッケージ: `jp.co.yumemi.quiz.droidkaigi`
