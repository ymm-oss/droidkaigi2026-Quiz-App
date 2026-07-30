---
name: droidkaigi-quiz-device-review
description: >-
  参加者 Android アプリを Android CLI で実際に動かし、機能要件・UI/UX・画面サイズ・
  アクセシビリティ・保守性をレビューして指摘を GitHub Issue 化する。
  「アプリをレビューして」「android cli でレビュー」「隠れる UI を調べて」
  「指摘を Issue にして」と依頼されたときに使用。
  コード diff のレビューは droidkaigi-quiz-review を使う。
---

# DroidKaigi Quiz — device review

実機 / エミュレータで参加者フローを操作し、SPEC との差分・UI 崩れ・保守性リスクを洗い出して Issue 化する。

**対象**: 参加者 Android（`:androidApp` + `composeApp` / `feature:quiz` / `feature:ranking`）。

| やりたいこと | 使うもの |
|--------------|----------|
| 動かして UI/UX と仕様を見る（このスキル） | Android CLI + `gh` |
| diff のコードレビュー | [droidkaigi-quiz-review](../droidkaigi-quiz-review/SKILL.md) |
| 手順どおりの動作確認だけ | [droidkaigi-quiz-verify](../droidkaigi-quiz-verify/SKILL.md) |
| スタッフ Desktop UI | [staff-pr-screenshots](../staff-pr-screenshots/SKILL.md) |

## Workflow

チェックリストをコピーして進捗管理する:

```
- [ ] 1 準備（ビルド・端末）
- [ ] 2 標準フローを操作して記録
- [ ] 3 エッジケース（画面サイズ・フォント・エラー）
- [ ] 4 コード側の裏取り
- [ ] 5 指摘をまとめてユーザーに確認
- [ ] 6 Issue 化
- [ ] 7 後片付け
```

## 1. 準備

このリポジトリ・環境固有の癖:

- `android` CLI はサンドボックス内では `~/.android/cli/bundles/lock` で失敗する → Shell は `required_permissions: ["all"]` で実行する
- `adb` は PATH にない → `export PATH="$HOME/Library/Android/sdk/platform-tools:$PATH"`
- 実機とエミュレータが同時に繋がる → `adb -s <serial>` / `android <cmd> --device=<serial>` を必ず付ける

```bash
./gradlew :androidApp:assembleFakeDebug   # 初回は Gradle 取得で数分かかる
android emulator list
android emulator start Medium_Phone_API_36.1
android run --device=<serial> --apks=androidApp/build/outputs/apk/fake/debug/androidApp-fake-debug.apk
```

`pm clear` や `uninstall` の後に `am start` すると `Activity class ... does not exist` になる。`android run` で入れ直す。

## 2. 標準フロー

Home →（ニックネーム入力）→ 単一選択 → 複数選択 → 並び替え → Result → Ranking → Home

各画面でスクショと layout を取る:

```bash
S=<serial>; OUT=docs/screenshots/review; mkdir -p $OUT
android screen capture --device=$S -o $OUT/03-quiz-q1.png
android layout --device=$S --pretty -o $OUT/03-quiz-q1.json
python3 .cursor/skills/droidkaigi-quiz-device-review/scripts/layout.py $OUT/03-quiz-q1.json
```

- スクショは**必ず目視**する。見切れ・コントラスト・重なりは layout に出ない
- タップ座標は layout の `center` を使う:
  ```bash
  adb -s $S shell input tap $(python3 .cursor/skills/droidkaigi-quiz-device-review/scripts/layout.py $OUT/03-quiz-q1.json --find 回答する)
  ```
- **初期表示に主要操作があるか**を毎問確認する。`回答する` が layout に出ない場合は画面外にある
- 状態変化後は layout を取り直す（座標は変わる）

## 3. エッジケース

| 観点 | 手順 |
|------|------|
| 短い画面 | `adb -s $S shell wm size 1080x1920` → アプリ再起動 → 確認 → `wm size reset` |
| Tablet / NavRail | `android emulator start Pixel_Tablet` で 600dp 以上の表示を確認 |
| フォント拡大 | `adb -s $S shell settings put system font_scale 1.3`（および `2.0`）→ 再起動して確認 |
| 入力検証 | 空ニックネームで開始 |
| 中断 | クイズ中に BACK / ナビゲーション移動 |
| キーボード | 入力欄フォーカス時に主要ボタンが隠れないか |

## 4. コード側の裏取り

UI で見えた挙動を実装で確認する。特に:

- ViewModel の失敗経路（`try` なしの suspend 呼び出しは詰まり・無限ローディングの原因）
- `isFinishing` のようなロック状態から復帰できるか
- prod のサイレントフォールバック（SPEC 違反）
- `@Ignore` されたテスト
- `docs/SPEC.md` と実装の差分

## 5. 指摘のまとめ

日本語・重大度順で報告する。UI 指摘には根拠のスクショと layout を紐付ける。

| 重要度 | 基準 |
|--------|------|
| High | 会場で完走・回答できない、情報が欠落する、本番障害時に復帰できない |
| Medium | 分かりにくい UI、仕様と実装の不一致、回帰検知の欠落 |
| Low | 見た目の粗、アクセシビリティ、技術的負債 |

Issue 化の前に、対象範囲・除外項目（デモ問題の内容など）・仕様判断（ランキング件数など）をユーザーに確認する。

## 6. Issue 化

- 重複を確認する: `gh issue list --state all --limit 200 --json number,title,url`
- ラベルは既存のみ使う: `bug` / `enhancement` / `documentation`
- 1 指摘 1 Issue、日本語。リポジトリに ISSUE_TEMPLATE はないので下記を使う

```markdown
## 概要
[何が起きるか。1〜3 文]

## 再現環境
- `fakeDebug`
- [端末 / 解像度 / density / font scale]

## 再現手順
1. ...

## 実際の結果
- ...

## 期待する結果
- ...

## 関連箇所
- `path/to/File.kt:123`

## 優先度
High / Medium / Low — [理由]
```

コードが根拠の指摘は「## 根拠」節に該当スニペットを入れる。

## 7. 後片付け

- `font_scale` を `1.0` に、`wm size` を `reset` に戻す
- 追加で起動したエミュレータを止める: `android emulator stop Pixel_Tablet`
- 作業用スクショ（`docs/screenshots/review/`）は削除する。成果物ではない
- ソースは変更しない。修正はユーザーの依頼があってから別作業で行う
