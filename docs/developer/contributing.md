# コントリビュート

誰でも PR を歓迎します。詳細はリポジトリの `docs/CONTRIBUTING.md` を参照してください。

## 相談先

| 手段 | 向いていること |
|------|----------------|
| Slack `#191_eve_droidkaigi` | 手早い質問・運営まわり |
| [GitHub Issues](https://github.com/ymm-oss/droidkaigi2026-Quiz-App/issues) | 設計議論・バグ・機能トラッキング |

## 作業の流れ

1. Issue / Slack で必要なら相談
2. `master` から feature ブランチ
3. fake ランタイムで実装・検証
4. テスト・手動確認
5. PR 作成（Approve 1 件でマージ可）

## PR チェックリスト（要点）

- [ ] `docs/SPEC.md` の該当 AC を満たす
- [ ] `jvmTest` を通す
- [ ] UI 変更時はスクショを PR 本文に埋め込む（参加者 Android / スタッフ Desktop それぞれ専用スキルあり）
- [ ] `quiz.runtime` / Firestore を触ったら prod 確認手順を PR に書く

## AI 駆動開発

このリポジトリは Cursor などの AI エディタでの開発を推奨しています。

- `AGENTS.md` — モジュール境界
- `.cursor/rules/` — MVI・テーマなどのルール
- `.cursor/skills/` — 実装・テスト・検証・レビュー・スクショ

UI PR のスクショ手順:

- 参加者 Android: `.cursor/skills/android-pr-screenshots/`
- スタッフ Desktop: `.cursor/skills/staff-pr-screenshots/`
