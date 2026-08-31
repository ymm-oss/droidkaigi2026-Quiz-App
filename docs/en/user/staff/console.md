# Console

Main screen after sign-in: quiz editing and ranking.

## Quiz management

Select a folder (quiz set), edit questions, and **publish folders independently**. Several folders can be published at once; participants pick them by name. The top bar toggles site-wide intake (`sitePublished`) and **参加者アプリ** copies/opens distribution URLs. **Preview** on the question list plays the participant UI. In preview you can switch **日本語 / English** to see the same question copy and chrome a participant would get from Home. Missing English fields fall back to Japanese.

<img src="/screenshots/staff/staff-participant-preview.png" alt="Participant preview (Japanese)" width="480" />
<img src="/screenshots/staff/staff-participant-preview-english.png" alt="Participant preview (English)" width="480" />

<img src="/screenshots/staff/02-console-quiz.png" alt="Quiz console" width="640" />

### Participant app links

Use **参加者アプリ** in the top bar to copy or open the URLs you share at the venue.

| Link | Use |
|------|-----|
| Web (browser) | Firebase Hosting participant app (production `https://ymm-droidkaigi26.web.app/`; Wasm `/staff` uses the current origin) |
| Android / Desktop | GitHub Releases (APK / DMG) |

<img src="/screenshots/staff/12-participant-links.png" alt="Participant app links" width="480" />

### Question editor

Configure single / multi / reorder questions and preview explanations.

<img src="/screenshots/staff/04-question-editor.png" alt="Question editor" width="640" />

### Create folder

<img src="/screenshots/staff/05-create-folder.png" alt="Create folder" width="480" />

### Edit folder name / description

The pencil icon on the selected sidebar row renames a folder and updates its description after creation. Questions are untouched.

<img src="/screenshots/staff/05b-edit-folder.png" alt="Edit folder" width="480" />

### Delete folder

The trash icon on the selected row deletes the folder after confirmation, including its questions and rankings. The last remaining folder cannot be deleted. Deleting a published folder removes it from the participant picker; other published folders stay published.

<img src="/screenshots/staff/05c-delete-folder.png" alt="Delete folder" width="480" />

### Publish confirmation

<img src="/screenshots/staff/06-publish-confirm.png" alt="Publish confirm" width="480" />

Adding or removing a folder from the participant picker is confirmed in a dialog. Multiple folders can stay published together.

Participants already answering stay on the folder they started, and their score is recorded in that folder's ranking. Publish changes only apply to participants who start afterwards.

### Other states

| State | Preview |
|-------|---------|
| Delete confirm | <img src="/screenshots/staff/07-delete-confirm.png" alt="Delete" width="360" /> |
| Empty questions | <img src="/screenshots/staff/08-empty-questions.png" alt="Empty" width="360" /> |
| No folder selected | <img src="/screenshots/staff/09-no-folder-selected.png" alt="None" width="360" /> |

## Ranking management

View and delete today's rankings for the selected folder. New completions appear in the list in realtime, so a manual refresh is not needed.

- **Delete one**: trash icon on a row → confirm dialog → delete
- **Delete all today**: 「すべて削除」→ confirm dialog → clear today's entries

<img src="/screenshots/staff/03-console-ranking.png" alt="Ranking management" width="640" />

| Confirm | Preview |
|---------|---------|
| Delete one | <img src="/screenshots/staff/10-ranking-delete-confirm.png" alt="Delete one confirm" width="360" /> |
| Clear today | <img src="/screenshots/staff/11-ranking-clear-confirm.png" alt="Clear today confirm" width="360" /> |
