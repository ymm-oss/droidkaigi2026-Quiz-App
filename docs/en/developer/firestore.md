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
  sitePublished           # site/reception open flag (default false)
  updatedAtEpochMillis?

folders/{folderId}/rankings/{entryId}
  nickname, score, completedAtEpochMillis, dateKey
```

Folder id and quiz-set id are **1:1**.

## Security (summary)

| Path | Read | Write |
|------|------|-------|
| `folders` / `appConfig` | Everyone | Authenticated staff |
| `rankings` | Everyone | `create` only (participant scores) |

## App mapping

| Repository | Firestore |
|------------|-----------|
| `RemoteQuizCatalogRepository` | `folders`, `appConfig/default` (`activeFolderId` / `sitePublished`) |
| `RemoteRankingRepository` | `folders/{id}/rankings` |

Participant load is two reads: `getActiveFolderId` → `getQuizSet`.
