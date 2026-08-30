# Scoring

## Formula

```
score = correctCount
```

The UI shows `correctCount / totalCount` (for example `2 / 3`). There is no time bonus for finishing early.

| Symbol | Meaning |
|--------|---------|
| `correctCount` | Number of correct answers |
| `totalCount` | Number of questions in the quiz set |
| `score` | Ranking sort key; same as `correctCount` |

## Ranking order

1. `score` (correct count) descending
2. Ties: `completedAtEpochMillis` ascending (earlier finish ranks higher)

Completion time is shown on ranking rows. Time spent on feedback overlays is not part of completion (final-answer submit).

## Correctness by type

| Type | Correct when |
|------|--------------|
| Single | Selected id equals `correctId` |
| Multiple | Selected set equals `correctIds` |
| Reorder | `orderedIds` equals `correctOrder` |

Covered by domain unit tests (`commonTest` / `jvmTest`).
