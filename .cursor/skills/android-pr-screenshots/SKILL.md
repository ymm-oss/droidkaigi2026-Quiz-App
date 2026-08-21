---
name: android-pr-screenshots
description: >-
  ユーザー向け Android アプリの UI 変更を PR に載せるとき、instrumented Compose スクショを撮って
  比較しやすい形で PR 本文へ埋め込む。Home / Quiz / Result / Ranking など画面の PR 作成時に使用。
---

# Android PR screenshots

ユーザー向けアプリ（`:feature:quiz` / `:feature:ranking` / `:core:ui` / `:composeApp` / `:androidApp`）の
**見た目や操作が変わる PR** では、必ず Android 実機レンダリングのスクショを PR 本文に埋め込む。

Desktop 側（`:feature:staff` など）は [staff-pr-screenshots](../staff-pr-screenshots/SKILL.md) を使う。
両方に関わる PR は両方載せる。

## When

次のいずれかに当てはまるとき **必須**:

- `feature/**` / `core/ui/**` の Compose UI・文言・レイアウト・アニメーション変更
- `composeApp/**` の画面遷移・スキャフォールド変更
- 新しいダイアログ・オーバーレイ・空状態・エラー表示の追加

ロジックのみ・DI のみ・ドキュメントのみなら不要。

## Capture

1. instrumented スクショテストで撮る。手順とハマりどころは
   [android-compose-screenshot](../android-compose-screenshot/SKILL.md) に従う。
   `android` CLI や `adb shell input tap` でのライブ操作キャプチャは使わない。
2. 出力先: `docs/screenshots/android/`
3. 実行例:

```bash
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
export PATH="$ANDROID_HOME/platform-tools:$PATH"
export ANDROID_SERIAL=emulator-5554

./gradlew :androidApp:connectedFakeDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=jp.co.yumemi.quiz.droidkaigi.QuizFeedbackScreenshotAndroidTest

cp "androidApp/build/outputs/connected_android_test_additional_output/fakeDebugAndroidTest/connected/"*/*.png \
  docs/screenshots/android/
```

4. 生成 PNG を読み、欠け・空画面・意図しない状態がないか確認する
5. PNG を `docs/screenshots/android/` にコミットする

## Embed on the PR（必須フォーマット）

**リポジトリ内 PNG の raw URL** を使い、**縮小 + 表比較**で載せる。

### URL

```text
https://raw.githubusercontent.com/<owner>/<repo>/<branch>/docs/screenshots/android/<file>.png
```

`<branch>` は PR の head ブランチ名。

### サイズ

- スマホ縦画面: `width="260"`〜`300`
- Markdown の `![]()` だけ（サイズ指定なし）は使わない

### 比較表（状態違い / Before・After）

```markdown
### Android UI

| 正解 | 不正解 | 最終問題 |
| --- | --- | --- |
| <img alt="Correct" src="https://raw.githubusercontent.com/<owner>/<repo>/<branch>/docs/screenshots/android/android-01-feedback-correct.png" width="260" /> | <img alt="Incorrect" src="https://raw.githubusercontent.com/<owner>/<repo>/<branch>/docs/screenshots/android/android-02-feedback-incorrect.png" width="260" /> | <img alt="Finish" src="https://raw.githubusercontent.com/<owner>/<repo>/<branch>/docs/screenshots/android/android-03-feedback-finish.png" width="260" /> |
```

JVM/Desktop のスクショも載せる PR では、**プラットフォームごとに節を分ける**（`### Android UI` /
`### Desktop UI`）。同じ表に混在させない。

### PR 本文の置き場

`### 具体的なもの`（または同等の「UI」節）に表を置く。チェックリストに Android UI スクショ済みを付ける。

## Checklist before opening / updating the PR

- [ ] 該当画面の PNG を instrumented テストで生成し、内容を目視確認した
- [ ] `docs/screenshots/android/` に置き、コミットした
- [ ] PR 本文に **width 付き `<img>` + 表** で埋め込んだ（リンクのみは不可）
- [ ] 状態違い（正解/不正解、Before/After など）は横並び比較にした
- [ ] Desktop 側も変わる PR なら staff スクショも別節で載せた
