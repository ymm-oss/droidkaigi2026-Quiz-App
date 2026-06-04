# DroidKaigi 2026 Quiz

Compose Multiplatform クイズアプリ（Android + Desktop）。選択式・並び替え問題と当日ランキング（モック）に対応。

## 構成

- `composeApp` — 共有 UI（Nav3 + adaptive）
- `androidApp` — Android エントリ
- `desktopApp` — Desktop エントリ
- `core:domain` / `core:data` / `core:ui`
- `feature:quiz` / `feature:ranking`

仕様: [docs/SPEC.md](docs/SPEC.md) · AI 向け: [AGENTS.md](AGENTS.md)

## ビルド

AGP 9.x + Gradle 9.4。Android アプリは `:androidApp` モジュールです。

```bash
./gradlew :androidApp:assembleDebug
```

## テスト

```bash
./gradlew :core:domain:jvmTest :core:data:jvmTest
./gradlew :androidApp:connectedDebugAndroidTest  # 要エミュレータ
```

## Desktop

```bash
./gradlew :desktopApp:run
```

## 手動確認

[docs/VERIFY.md](docs/VERIFY.md)
