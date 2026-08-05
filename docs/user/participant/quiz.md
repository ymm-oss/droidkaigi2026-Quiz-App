# クイズ

問題に回答する画面です。進捗（例: `1 / 3`）とプログレスバーが表示されます。

## 問題形式ごとの UI

### 単一選択

選択肢から **1 つ** を選び、「回答する」で提出します。

<img src="/screenshots/pr-prompt-markdown/quiz-q1-single-choice.png" alt="単一選択の例" width="280" />

### 複数選択

正しいと思う選択肢を **すべて** 選びます。選択中はハイライトされます。

<img src="/screenshots/pr-prompt-markdown/quiz-q2-multiple-choice.png" alt="複数選択の例" width="280" />

### 並び替え

右端のドラッグハンドルで項目の順序を並べ替え、「回答する」で提出します。

<img src="/screenshots/pr-prompt-markdown/quiz-q3-reorder.png" alt="並び替えの例" width="280" />

## 回答後のフィードバック

提出直後に全画面オーバーレイで正誤が表示されます。

| 状態 | 表示例 |
|------|--------|
| 正解 | 「正解！」と次の問題へ進むボタン |
| 不正解 | 不正解フィードバックと次へ進む導線 |
| 最終問題の正解後 | 結果画面へ進む導線 |

<img src="/screenshots/android/android-01-feedback-correct.png" alt="正解フィードバック" width="280" />
<img src="/screenshots/android/android-02-feedback-incorrect.png" alt="不正解フィードバック" width="280" />
<img src="/screenshots/android/android-03-feedback-finish.png" alt="最終問題後のフィードバック" width="280" />

フィードバック表示中の時間は、採点の経過時間には含まれません。
