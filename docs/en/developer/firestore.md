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

staffAppRelease/latest    # staff Desktop latest release (auth required)
  version, versionCode, storagePath, sha256, releaseNotes, publishedAtEpochMillis?

folders/{folderId}/rankings/{entryId}
  nickname, score, completedAtEpochMillis, dateKey
```

Folder id and quiz-set id are **1:1**.

### Storage (staff DMG)

`releases/staff-desktop/{version}.dmg` — authenticated staff read only.

## Security (summary)

| Path | Read | Write |
|------|------|-------|
| `folders` / `appConfig` | Everyone | Authenticated staff |
| `rankings` | Everyone | `create` (participant scores); `delete` authenticated staff; no `update` |
| `staffAppRelease` | Authenticated staff | Clients denied (CD / Admin SDK) |
| Storage `releases/staff-desktop/**` | Authenticated staff | Clients denied (CD / Admin SDK) |

## App mapping

| Repository | Firestore / Storage |
|------------|---------------------|
| `RemoteQuizCatalogRepository` | `folders`, `appConfig/default` (`activeFolderId` / `sitePublished`) |
| `RemoteRankingRepository` | `folders/{id}/rankings` |
| `RemoteStaffAppReleaseRepository` | `staffAppRelease/latest` + Storage DMG |

Participants listen to `appConfig/default` and read `getQuizSet` for the published folder at start. In-progress quizzes do not swap questions. Ranking screens listen to today's `rankings` for the played or selected folder (`dateKey` equality, sorted client-side).

## CD (rules)

Pushes to `master` that change `firestore.rules` / `storage.rules` deploy via GitHub Actions. Required secret: `FIREBASE_SERVICE_ACCOUNT` (setup: `docs/DEVELOPMENT.md`, section on CD for Firebase rules).
