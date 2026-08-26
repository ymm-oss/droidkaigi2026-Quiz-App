# Firestore

`quiz.runtime=prod` 時のバックエンドです。セットアップ手順は `docs/DEVELOPMENT.md`、詳細スキーマは `docs/FIRESTORE.md` を参照してください。

## コレクション概要

```
folders/{folderId}
  name, description, sortOrder, title
  questions: array<map>   # single_choice | multiple_choice | reorder
  updatedAtEpochMillis?

appConfig/default
  publishedFolderIds      # 参加者向け公開フォルダ（複数）
  activeFolderId          # 互換用（公開リストの先頭）
  sitePublished           # サイト／受付の公開可否（既定 false）
  updatedAtEpochMillis?

staffAppRelease/latest    # スタッフ Desktop 最新版（認証必須）
  version, versionCode, storagePath, sha256, releaseNotes, publishedAtEpochMillis?

folders/{folderId}/rankings/{entryId}
  nickname, score, completedAtEpochMillis, dateKey
```

フォルダ ID とクイズセット ID は **1:1** です。

### Storage（スタッフ DMG）

`releases/staff-desktop/{version}.dmg` — 認証済みスタッフのみ読取。

## セキュリティ（要点）

| パス | 読取 | 書込 |
|------|------|------|
| `folders` / `rankings` | 未認証は公開フォルダのみ。スタッフは認証済みで全件 | フォルダ書き込みは認証済みスタッフ。ランキング `create` は公開フォルダ。`delete` は認証済みスタッフ。`update` 不可 |
| `appConfig` | 全員 | 認証済みスタッフのみ |
| `staffAppRelease` | 認証済みスタッフ | クライアント不可（CD / Admin SDK） |
| Storage `releases/staff-desktop/**` | 認証済みスタッフ | クライアント不可（CD / Admin SDK） |

## アプリからのマッピング

| Repository | Firestore / Storage |
|------------|---------------------|
| `RemoteQuizCatalogRepository` | `folders`, `appConfig/default`（`publishedFolderIds` / `activeFolderId` / `sitePublished`） |
| `RemoteRankingRepository` | `folders/{id}/rankings` |
| `RemoteStaffAppReleaseRepository` | `staffAppRelease/latest` + Storage DMG |

参加者の問題取得は公開フォルダ一覧のあと、開始時に選んだフォルダの `getQuizSet` です。

## CD（ルール）

`master` への `firestore.rules` / `storage.rules` 変更は GitHub Actions が自動デプロイします。必要な Secret は `FIREBASE_SERVICE_ACCOUNT`（手順: [DEVELOPMENT.md](../DEVELOPMENT.md#cdmaster-マージ時のルール自動デプロイ)）。
