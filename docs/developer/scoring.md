# 採点ロジック

## 式

```
score = correctCount
```

表示は `correctCount / totalCount`（例: `2 / 3`）。早期回答の時間ボーナスはない。

| 記号 | 意味 |
|------|------|
| `correctCount` | 正解した問題数 |
| `totalCount` | そのクイズセットの問題数 |
| `score` | ランキングの並び用。`correctCount` と同じ |

## ランキングの並び

1. `score`（正解数）の降順
2. 同点なら `completedAtEpochMillis` の昇順（先に完了した方が上位）

完了日時はランキング行に表示する。フィードバック閲覧中の時間は完了時刻に含めない（最終回答の提出時点）。

## 問題形式ごとの正誤

| 形式 | 正解条件 |
|------|----------|
| 単一選択 | 選択 ID が `correctId` と一致 |
| 複数選択 | 選択集合が `correctIds` と完全一致 |
| 並び替え | `orderedIds` が `correctOrder` と完全一致 |

ドメイン層のユニットテスト（`commonTest` / `jvmTest`）で検証してください。
