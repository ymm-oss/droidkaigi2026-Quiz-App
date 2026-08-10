# 運営コンソール

ログイン後のメイン画面です。クイズ編集とランキング参照を切り替えます。

## クイズ管理

フォルダ（クイズセット）を選び、問題一覧の確認・編集・公開切替を行います。トップバーではサイト全体の受付公開（`sitePublished`）を切り替え、問題一覧ヘッダの「プレビュー」で参加者画面を試遊できます。

<img src="/screenshots/staff/02-console-quiz.png" alt="クイズ管理コンソール" width="640" />

### 問題エディタ

形式（単一選択 / 複数選択 / 並び替え）に応じた設定と、解説のプレビューができます。

<img src="/screenshots/staff/04-question-editor.png" alt="問題エディタ" width="640" />

### フォルダ作成

用途（日付・レベルなど）に応じてフォルダを追加できます。

<img src="/screenshots/staff/05-create-folder.png" alt="フォルダ作成" width="480" />

### フォルダの名前・説明を編集

サイドバーで選択中のフォルダ行に出る鉛筆アイコンから、作成後でも名前と説明を変更できます。問題の内容には影響しません。

<img src="/screenshots/staff/05b-edit-folder.png" alt="フォルダ編集" width="480" />

### フォルダの削除

選択中フォルダ行のゴミ箱アイコンから削除できます。確認ダイアログのあと、問題とランキングも含めて削除されます。最後の 1 件は削除できません。公開中フォルダを消した場合は、別のフォルダが公開対象になります。

<img src="/screenshots/staff/05c-delete-folder.png" alt="フォルダ削除" width="480" />

### 公開の確認

参加者アプリが読み込む「公開中フォルダ」を切り替えるとき、確認ダイアログが表示されます。

<img src="/screenshots/staff/06-publish-confirm.png" alt="公開確認" width="480" />

回答中の参加者は、開始時のフォルダのまま最後まで回答でき、スコアもそのフォルダのランキングに記録されます。切り替えが効くのは、切り替え後に新しく開始する参加者からです。

### その他の状態

| 状態 | イメージ |
|------|----------|
| 削除確認 | <img src="/screenshots/staff/07-delete-confirm.png" alt="削除確認" width="360" /> |
| 問題が空 | <img src="/screenshots/staff/08-empty-questions.png" alt="問題なし" width="360" /> |
| フォルダ未選択 | <img src="/screenshots/staff/09-no-folder-selected.png" alt="未選択" width="360" /> |

## ランキング管理

選択中フォルダの当日ランキングを確認・削除できます。

- **個別削除**: 各行のゴミ箱アイコン → 確認ダイアログ後に削除
- **一括削除**: 「すべて削除」→ 確認ダイアログ後に本日分をすべて削除

<img src="/screenshots/staff/03-console-ranking.png" alt="ランキング管理" width="640" />

| 確認 | イメージ |
|------|----------|
| 個別削除 | <img src="/screenshots/staff/10-ranking-delete-confirm.png" alt="個別削除確認" width="360" /> |
| すべて削除 | <img src="/screenshots/staff/11-ranking-clear-confirm.png" alt="一括削除確認" width="360" /> |
