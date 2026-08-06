# Firestore

Backend for `quiz.runtime=prod`. Setup: `docs/DEVELOPMENT.md`. Schema: `docs/FIRESTORE.md`.

## Collections

```
folders/{folderId}
  name, description, sortOrder, title
  questions: array<map>   # single_choice | multiple_choice | reorder
  updatedAtEpochMillis?

appConfig/default
  activeFolderId
  updatedAtEpochMillis?

folders/{folderId}/rankings/{entryId}
  nickname, score, completedAtEpochMillis, dateKey
```

Folder id and quiz-set id are **1:1**.

## Security (summary)

| Path | Read | Write |
|------|------|-------|
| `folders` / `appConfig` | Everyone | Authenticated staff |
| `rankings` | Everyone | `create` (participant scores); `delete` authenticated staff; no `update` |

## App mapping

| Repository | Firestore |
|------------|-----------|
| `RemoteQuizCatalogRepository` | `folders`, `appConfig/default` |
| `RemoteRankingRepository` | `folders/{id}/rankings` |

Participant load is two reads: `getActiveFolderId` → `getQuizSet`.

## CD (rules)

Pushes to `master` that change `firestore.rules` deploy via GitHub Actions. Required secret: `FIREBASE_SERVICE_ACCOUNT` (setup: `docs/DEVELOPMENT.md`, section on CD for Firestore rules).
