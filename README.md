# DroidKaigi 2026 Quiz

DroidKaigi 2026 向けの **Compose Multiplatform** プロジェクト。会場で参加者が解く **クイズアプリ** と、運営 PC 向けの **管理者（スタッフ）アプリ** の 2 系統で構成する。

## アプリ概要

### クイズアプリ（参加者向け）

Android（`:androidApp`）と Desktop（`:desktopApp`）で配布・実行する。ニックネームを入力してクイズに挑戦し、当日のランキングを確認する。

| 機能 | 内容 |
|------|------|
| **三種のクイズ** | **単一選択**・**複数選択**・**並び替え**（ドラッグで順序変更） |
| **ランキング** | 当日の Top N と自分の行のハイライト（採点は正解数 + 時間ボーナス） |

画面の流れ: Home → Quiz → Result → Ranking（詳細は [docs/SPEC.md](docs/SPEC.md)）。

### 管理者アプリ（スタッフ向け）

Desktop のみ（`:staffDesktopApp`）。会場運営が問題セットと公開状態を管理する。

| 機能 | 内容 |
|------|------|
| **クイズの管理** | 問題の追加・編集、解説（Markdown 風プレビュー）、形式ごとの設定 |
| **フォルダの管理** | フォルダ単位でクイズセットを分ける（**日付**・**難易度（レベル）** など用途に応じたセット）。参加者向けに公開するフォルダ（アクティブフォルダ）の切り替え |
| **ランキングの参照** | フォルダごとの当日ランキング確認 |

本番では Firebase Authentication のあと **Firestore** に保存する。開発時はインメモリで UI を検証できる（[docs/DEVELOPMENT.md](docs/DEVELOPMENT.md)）。

### Web（Wasm）について

`:wasmApp` は **ビルドターゲットとして追加済み**だが、**現時点では運用・配布に使っていない**。将来、QR コード経由でのブラウザ参加などを検討する余地用。Firebase Hosting 設定は [docs/DEVELOPMENT.md#firebase-セットアップ](docs/DEVELOPMENT.md#firebase-セットアップ) を参照。

### データ（本番と開発）

本番では問題・ランキングとも **Firestore 等のリモート必須**（オフライン完走は想定しない）。開発時のみ `quiz.runtime=fake` で同梱 JSON とインメモリランキングによりオフライン検証できる。

### 関連ドキュメント

| ドキュメント | 内容 |
|--------------|------|
| [docs/CONTRIBUTING.md](docs/CONTRIBUTING.md) | 参加方法・PR/Issue・レビュー・AI 開発 |
| [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) | 環境構築・ビルド・fake/prod/local・**Firebase セットアップ**・[Emulator 起動](docs/DEVELOPMENT.md#firebase-emulatorlocal) |
| [docs/SPEC.md](docs/SPEC.md) | 画面・問題形式・採点 |
| [docs/FIRESTORE.md](docs/FIRESTORE.md) | 本番 DB 構成・ルール・シード |
| [docs/VERIFY.md](docs/VERIFY.md) | 手動確認手順 |
| [AGENTS.md](AGENTS.md) | モジュール境界・AI 向け |

---

## 開発者向け

**誰でも PR 歓迎**です。疑問・不具合・新機能は [GitHub Issue](https://github.com/h-ideura/droidkaigi2026-Quiz-App/issues) または PR、相談は [Slack `#191_eve_droidkaigi`](https://accenture.enterprise.slack.com/archives/C0AMEMFCL4T) でどうぞ。

| やりたいこと | 読むドキュメント |
|--------------|------------------|
| 初めて参加する | [docs/CONTRIBUTING.md](docs/CONTRIBUTING.md) |
| ビルド・実行する | [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md)（既定は `quiz.runtime=fake` でオフライン可） |
| Firestore を Emulator で結合する | [DEVELOPMENT の Emulator 節](docs/DEVELOPMENT.md#firebase-emulatorlocal)（`firebase emulators:start --import=./emulator-data`） |
| 仕様を確認する | [docs/SPEC.md](docs/SPEC.md) |
| AI（Cursor）で実装する | [CONTRIBUTING の AI 節](docs/CONTRIBUTING.md#ai-駆動開発推奨) + [AGENTS.md](AGENTS.md) |

クイックスタート（fake）:

```bash
./gradlew :androidApp:assembleFakeDebug
./gradlew :staffDesktopApp:run
```
