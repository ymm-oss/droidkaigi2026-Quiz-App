# Scoring

## Formula

```
questionAccuracy ∈ [0, 1]
score = round(average(questionAccuracy) × 100)   # 0–100
```

There is no time bonus. Result and ranking show `score%`. Fully correct question count is secondary.

| Symbol | Meaning |
|--------|---------|
| `questionAccuracy` | Closeness for one question (1 when exact) |
| `correctCount` | Questions with an exact match (same as feedback) |
| `totalCount` | Questions in the quiz set |
| `score` | Ranking sort key: 0–100 accuracy |

## Closeness by type

| Type | Fully correct | Partial credit |
|------|---------------|----------------|
| Single | Selected id equals `correctId` → 1 | Otherwise 0 |
| Multiple | Selected set equals `correctIds` → 1 | Jaccard: `|sel ∩ correct| / |sel ∪ correct|` |
| Reorder | `orderedIds` equals `correctOrder` → 1 | Fraction of item pairs in the right relative order (Kendall). Closer orders score higher |

## Ranking order

1. `score` (accuracy) descending
2. Ties: `completedAtEpochMillis` ascending (earlier finish ranks higher)

Completion time is shown on ranking rows. Time spent on feedback overlays is not part of completion (final-answer submit).

Covered by domain unit tests (`commonTest` / `jvmTest`).
