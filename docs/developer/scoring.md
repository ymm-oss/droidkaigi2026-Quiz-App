# 採点ロジック

## 式

```
score = correctCount * 100 + timeBonus
timeBonus = (50 - elapsedSeconds).coerceIn(0, 50)
```

| 記号 | 意味 |
|------|------|
| `correctCount` | 正解した問題数 |
| `elapsedSeconds` | クイズ開始〜**最終回答の提出時点**までの秒数 |
| `timeBonus` | 0〜50。速いほど高い |

## 時間の境界

- **含む**: 問題表示〜各回答の提出まで
- **含めない**: 回答後フィードバックの閲覧時間

## 問題形式ごとの正誤

| 形式 | 正解条件 |
|------|----------|
| 単一選択 | 選択 ID が `correctId` と一致 |
| 複数選択 | 選択集合が `correctIds` と完全一致 |
| 並び替え | `orderedIds` が `correctOrder` と完全一致 |

ドメイン層のユニットテスト（`commonTest` / `jvmTest`）で検証してください。
