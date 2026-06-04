# DroidKaigi 2026 Quiz

Compose Multiplatform クイズアプリ（Android + Desktop）。選択式・並び替え問題と当日ランキング（モック）に対応。

## 構成

- `composeApp` — Nav3 + adaptive UI
- `core:domain` / `core:data` / `core:ui`
- `feature:quiz` / `feature:ranking`

仕様: [docs/SPEC.md](docs/SPEC.md) · AI 向け: [AGENTS.md](AGENTS.md)

## ビルド

```bash
./gradlew :composeApp:assembleDebug
```

## テスト

```bash
./gradlew :core:domain:jvmTest :core:data:jvmTest
./gradlew :composeApp:connectedDebugAndroidTest  # 要エミュレータ
```

## Desktop

```bash
./gradlew :composeApp:run
```

## 手動確認

[docs/VERIFY.md](docs/VERIFY.md)
