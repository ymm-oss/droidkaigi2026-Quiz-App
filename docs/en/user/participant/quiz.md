# Quiz

Answer questions with progress (e.g. `1 / 3`) and a progress bar.

## UI by question type

### Single choice

Pick **one** option and submit.

<img src="/screenshots/pr-prompt-markdown/quiz-q1-single-choice.png" alt="Single choice example" width="280" />

### Multiple choice

Select **all** correct options (highlighted while selected).

<img src="/screenshots/pr-prompt-markdown/quiz-q2-multiple-choice.png" alt="Multiple choice example" width="280" />

### Reorder

Drag the handle on the right to reorder, then submit.

<img src="/screenshots/pr-prompt-markdown/quiz-q3-reorder.png" alt="Reorder example" width="280" />

## Feedback after submit

| State | What you see |
|-------|----------------|
| Correct | “Correct!”, the correct answer, an explanation, and continue |
| Incorrect | “Incorrect”, the correct answer, an explanation, and continue |
| Last question | Feedback, the correct answer, an explanation, and continue to Result |

<img src="/screenshots/android/android-01-feedback-correct.png" alt="Correct feedback" width="280" />
<img src="/screenshots/android/android-02-feedback-incorrect.png" alt="Incorrect feedback" width="280" />
<img src="/screenshots/android/android-03-feedback-finish.png" alt="Finishing feedback" width="280" />

Explanations are rendered as Markdown. Long explanations can be scrolled, while the explanation section is omitted when none is registered.

Time spent on the feedback overlay is **not** included in scoring elapsed time.
