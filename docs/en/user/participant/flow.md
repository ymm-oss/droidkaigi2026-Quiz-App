# Screen flow

Basic flow of the participant app:

```
Home → Quiz (multiple questions) → Result → Ranking
```

| Screen | Role |
|--------|------|
| [Home](/en/user/participant/home) | Nickname, language, published quiz type, start |
| [Quiz](/en/user/participant/quiz) | Answer UI + feedback |
| [Result](/en/user/participant/result) | Correct count and score |
| [Ranking](/en/user/participant/ranking) | Today's leaders and your place |

Wide layouts (e.g. tablets) show a navigation rail.

## Question types

| Type | Interaction | Correct when |
|------|-------------|--------------|
| **Single choice** | Pick one, submit | Matches the correct option |
| **Multiple choice** | Pick all that apply, submit | Exactly matches the correct set |
| **Reorder** | Drag handles, submit | Exactly matches the correct order |

After submit, a full-screen feedback overlay appears; continue to the next question or to Result.
