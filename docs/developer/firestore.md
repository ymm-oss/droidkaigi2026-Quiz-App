# Firestore

`quiz.runtime=prod` 時のバックエンドです。セットアップ手順は `docs/DEVELOPMENT.md`、詳細スキーマは `docs/FIRESTORE.md` を参照してください。

## コレクション概要

```
folders/{folderId}
  name, description, sortOrder, title
  questions: array<map>   # single_choice | multiple_choice | reorder
  updatedAtEpochMillis?

appConfig/default
  activeFolderId          # 参加者向け公開中フォルダ
  sitePublished           # サイト／受付の公開可否（既定 false）
  updatedAtEpochMillis?

staffAppRelease/latest    # スタッフ Desktop 最新版（認証必須）
  version, versionCode, storagePath, sha256, releaseNotes, publishedAtEpochMillis?

folders/{folderId}/rankings/{entryId}
  nickname, score, totalCount, completedAtEpochMillis, dateKey
```

フォルダ ID とクイズセット ID は **1:1** です。

### Storage（スタッフ DMG）

`releases/staff-desktop/{version}.dmg` — 認証済みスタッフのみ読取。

## セキュリティ（要点）

| パス | 読取 | 書込 |
|------|------|------|
| `folders` / `appConfig` | 全員 | 認証済みスタッフのみ |
| `rankings` | 全員 | `create`（参加者スコア）。`delete` は認証済みスタッフのみ。`update` 不可 |
| `staffAppRelease` | 認証済みスタッフ | クライアント不可（CD / Admin SDK） |
| Storage `releases/staff-desktop/**` | 認証済みスタッフ | クライアント不可（CD / Admin SDK） |

## アプリからのマッピング

| Repository | Firestore / Storage |
|------------|---------------------|
| `RemoteQuizCatalogRepository` | `folders`, `appConfig/default`（`activeFolderId` / `sitePublished`） |
| `RemoteRankingRepository` | `folders/{id}/rankings` |
| `RemoteStaffAppReleaseRepository` | `staffAppRelease/latest` + Storage DMG |

参加者は `appConfig/default` を listen し、開始時に公開中フォルダの `getQuizSet` を読みます。プレイ中は問題を差し替えません。

## CD（ルール）

`master` への `firestore.rules` / `storage.rules` 変更は GitHub Actions が自動デプロイします。必要な Secret は `FIREBASE_SERVICE_ACCOUNT`（手順: [DEVELOPMENT.md](../DEVELOPMENT.md#cdmaster-マージ時のルール自動デプロイ)）。
