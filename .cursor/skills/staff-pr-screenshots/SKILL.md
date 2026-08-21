---
name: staff-pr-screenshots
description: >-
  管理者（スタッフ）アプリの UI 変更を PR に載せるとき、JVM Compose スクショを撮って
  比較しやすい形で PR 本文へ埋め込む。スタッフ画面・認証・運営コンソールの PR 作成時に使用。
---

# Staff PR screenshots

管理者アプリ（`:feature:staff` / `:staffComposeApp` / `:staffDesktopApp`）の **見た目や操作が変わる PR** では、必ずスクショを PR 本文に埋め込む。

## When

次のいずれかに当てはまるとき **必須**:

- `feature/staff/**` の Compose UI・文言・レイアウト変更
- `staffComposeApp/**` / `staffDesktopApp/**` の UI 変更
- Fake / prod で表示が分岐するスタッフ UI（認証ボタンなど）

テキストのみ・DI のみ・ドキュメントのみなら不要。

## Capture

1. 既存ハーネスを使う（新規は [jvm-compose-screenshot](../jvm-compose-screenshot/SKILL.md)）
2. 出力先: `docs/screenshots/staff/`（`staff.screenshot.dir`）
3. 実行例:

```bash
xvfb-run --auto-servernum ./gradlew :feature:staff:jvmTest \
  --tests 'jp.co.yumemi.quiz.droidkaigi.feature.staff.auth.StaffAuthContentJvmUiTest' \
  --tests 'jp.co.yumemi.quiz.droidkaigi.feature.staff.StaffScreenshotJvmUiTest'
```

4. 生成 PNG を読み、欠け・空画面・意図しない状態がないか確認する
5. レビュー用なら PNG をコミットする（`docs/screenshots/staff/`）

## Embed on the PR（必須フォーマット）

Cursor artifact のインライン表示はチーム設定依存のため使わない。  
**リポジトリ内 PNG の raw URL** を使い、**縮小 + 表比較**で載せる。

### URL

```text
https://raw.githubusercontent.com/<owner>/<repo>/<branch>/docs/screenshots/staff/<file>.png
```

`<branch>` は PR の head ブランチ名。

### サイズ

- ログインなど縦長: `width="280"`〜`320`
- コンソールなど横長: `width="420"`〜`480`
- Markdown の `![]()` だけ（サイズ指定なし）は使わない

### 比較表（Before/After や Fake/Prod）

```markdown
### UI

| Fake（ワンクリックあり） | Prod（なし） |
| --- | --- |
| <img alt="Fake staff auth" src="https://raw.githubusercontent.com/<owner>/<repo>/<branch>/docs/screenshots/staff/staff-auth-fake-quick-login.png" width="300" /> | <img alt="Prod staff auth" src="https://raw.githubusercontent.com/<owner>/<repo>/<branch>/docs/screenshots/staff/staff-auth-prod-no-quick-login.png" width="300" /> |
```

単画面だけのときは 1 列でもよいが、**分岐・差分があるなら表で並べる**。

### PR 本文の置き場

`### 具体的なもの`（または同等の「UI」節）に表を置く。チェックリストにスタッフ UI スクショ済みを付ける。

## Checklist before opening / updating the PR

- [ ] 該当画面の PNG を生成し、内容を目視確認した
- [ ] `docs/screenshots/staff/` に置き、必要ならコミットした
- [ ] PR 本文に **width 付き `<img>` + 表** で埋め込んだ（リンクのみは不可）
- [ ] Fake/prod や Before/After がある場合は横並び比較にした
