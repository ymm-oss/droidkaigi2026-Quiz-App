# 運営コンソール

ログイン後のメイン画面です。クイズ編集とランキング参照を切り替えます。

## クイズ管理

フォルダ（クイズセット）を選び、問題一覧の確認・編集・**フォルダごとの公開**を行います。複数フォルダを同時に公開でき、参加者アプリでは名称で選べます。トップバーではサイト全体の受付公開（`sitePublished`）を切り替え、**参加者アプリ**から配布 URL をコピー／ブラウザで開けます。問題一覧ヘッダの「プレビュー」で参加者画面を試遊できます。プレビューでは **日本語 / English** を切り替えて、参加者が Home で言語を選んだときと同じ問題文・UI を確認できます。未入力の英語項目は日本語が表示されます。

<img src="/screenshots/staff/staff-participant-preview.png" alt="参加者プレビュー（日本語）" width="480" />
<img src="/screenshots/staff/staff-participant-preview-english.png" alt="参加者プレビュー（English）" width="480" />

<img src="/screenshots/staff/02-console-quiz.png" alt="クイズ管理コンソール" width="640" />

### 参加者アプリのリンク

トップバーの「参加者アプリ」から、会場で配布する URL をコピーしたりブラウザで開いたりできます。

| リンク | 用途 |
|--------|------|
| Web（ブラウザ） | Firebase Hosting の参加者アプリ（本番は `https://ymm-droidkaigi26.web.app/`。Wasm `/staff` では今開いている origin） |
| Android / Desktop | GitHub Releases（APK / DMG） |

<img src="/screenshots/staff/12-participant-links.png" alt="参加者アプリのリンク" width="480" />

### 問題エディタ

形式（単一選択 / 複数選択 / 並び替え）に応じた設定と、解説のプレビューができます。

<img src="/screenshots/staff/04-question-editor.png" alt="問題エディタ" width="640" />

### フォルダ作成

用途（日付・レベルなど）に応じてフォルダを追加できます。

<img src="/screenshots/staff/05-create-folder.png" alt="フォルダ作成" width="480" />

### 管理用の名前・説明を編集

サイドバーで選択中のフォルダ行に出る鉛筆アイコンから、作成後でも内部管理用の名前と説明を変更できます。問題の内容には影響しません。

<img src="/screenshots/staff/05b-edit-folder.png" alt="フォルダ編集" width="480" />

### フォルダの削除

選択中フォルダ行のゴミ箱アイコンから削除できます。確認ダイアログのあと、問題とランキングも含めて削除されます。最後の 1 件は削除できません。公開中フォルダを消した場合は、参加者の選択肢から外れます（他の公開フォルダは残ります）。

<img src="/screenshots/staff/05c-delete-folder.png" alt="フォルダ削除" width="480" />

### 公開情報の設定

公開時に、参加者へ表示する公開名と公開説明を管理用情報とは別に設定できます。「内部の名称・説明をそのまま公開する」を選ぶと管理用情報へ追従します。以前入力した公開情報は消えず、チェックを外すと再利用できます。公開中も公開情報を編集でき、公開を外しても内容は保存されます。複数フォルダを同時に公開できます。

| 公開情報を入力 | 管理情報をそのまま使用 |
|---|---|
| <img src="/screenshots/staff/06-publish-confirm.png" alt="公開情報を入力" width="420" /> | <img src="/screenshots/staff/06b-publish-internal-info.png" alt="管理情報をそのまま公開" width="420" /> |

回答中の参加者は、開始時のフォルダのまま最後まで回答でき、スコアもそのフォルダのランキングに記録されます。公開の追加・解除が効くのは、その後に新しく開始する参加者からです。

### その他の状態

| 状態 | イメージ |
|------|----------|
| 削除確認 | <img src="/screenshots/staff/07-delete-confirm.png" alt="削除確認" width="360" /> |
| 問題が空 | <img src="/screenshots/staff/08-empty-questions.png" alt="問題なし" width="360" /> |
| フォルダ未選択 | <img src="/screenshots/staff/09-no-folder-selected.png" alt="未選択" width="360" /> |

## ランキング管理

選択中フォルダの当日ランキングを確認・削除できます。新しい完了は一覧にリアルタイムで追加されるため、手動の更新は不要です。

- **個別削除**: 各行のゴミ箱アイコン → 確認ダイアログ後に削除
- **一括削除**: 「すべて削除」→ 確認ダイアログ後に本日分をすべて削除

<img src="/screenshots/staff/03-console-ranking.png" alt="ランキング管理" width="640" />

| 確認 | イメージ |
|------|----------|
| 個別削除 | <img src="/screenshots/staff/10-ranking-delete-confirm.png" alt="個別削除確認" width="360" /> |
| すべて削除 | <img src="/screenshots/staff/11-ranking-clear-confirm.png" alt="一括削除確認" width="360" /> |
