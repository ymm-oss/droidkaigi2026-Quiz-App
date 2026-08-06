# Scoring

## Formula

```
score = correctCount * 100 + timeBonus
timeBonus = (50 - elapsedSeconds).coerceIn(0, 50)
```

| Symbol | Meaning |
|--------|---------|
| `correctCount` | Number of correct answers |
| `elapsedSeconds` | From quiz start to **final answer submit** |
| `timeBonus` | 0–50; faster is higher |

## Time boundaries

- **Included**: question view through each submit
- **Excluded**: time spent on feedback overlays

## Correctness by type

| Type | Correct when |
|------|--------------|
| Single | Selected id equals `correctId` |
| Multiple | Selected set equals `correctIds` |
| Reorder | `orderedIds` equals `correctOrder` |

Covered by domain unit tests (`commonTest` / `jvmTest`).
