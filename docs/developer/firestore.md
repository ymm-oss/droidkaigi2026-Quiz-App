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

folders/{folderId}/rankings/{entryId}
  nickname, score, completedAtEpochMillis, dateKey
```

フォルダ ID とクイズセット ID は **1:1** です。

## セキュリティ（要点）

| パス | 読取 | 書込 |
|------|------|------|
| `folders` / `appConfig` | 全員 | 認証済みスタッフのみ |
| `rankings` | 全員 | `create`（参加者スコア）。`delete` は認証済みスタッフのみ。`update` 不可 |

## アプリからのマッピング

| Repository | Firestore |
|------------|-----------|
| `RemoteQuizCatalogRepository` | `folders`, `appConfig/default`（`activeFolderId` / `sitePublished`） |
| `RemoteRankingRepository` | `folders/{id}/rankings` |

参加者の問題取得は `getActiveFolderId` → `getQuizSet` の **2 読み取り**で足ります。

## CD（ルール）

`master` への `firestore.rules` 変更は GitHub Actions が自動デプロイします。必要な Secret は `FIREBASE_SERVICE_ACCOUNT`（手順: [DEVELOPMENT.md](../DEVELOPMENT.md#cdmaster-マージ時のルール自動デプロイ)）。
