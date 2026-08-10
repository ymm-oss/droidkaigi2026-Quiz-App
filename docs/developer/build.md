# ビルド・実行

前提: AGP 9.x + Gradle 9.4、JDK 17+（Desktop）。

## 参加者 — Android

```bash
./gradlew :androidApp:assembleFakeDebug    # 開発
./gradlew :androidApp:assembleProdDebug    # 本番（要 Firebase 設定）
```

Android Studio では **Build Variants** で `fakeDebug` / `prodDebug` を切り替え、Rebuild してから実行します。

## 参加者 — Desktop

```bash
./gradlew :desktopApp:run
./gradlew :desktopApp:run -Pquiz.runtime=prod
```

## スタッフ — Desktop

```bash
./gradlew :staffDesktopApp:run
./gradlew :staffDesktopApp:run -Pquiz.runtime=prod
```

Run Configuration: `staffDesktop[Fake]` / `staffDesktop[Prod]`。

| ランタイム | 認証 |
|------------|------|
| fake | ローカル固定アカウント（デモワンクリック可） |
| prod | Firebase Auth（運営共有のメール / パスワード） |

prod ではログイン後に最新版チェックがあり、古い場合は DMG をダウンロードできます（CD: `Release Staff Desktop`）。

## Web（Wasm）

```bash
./gradlew :wasmApp:wasmJsBrowserDevelopmentRun
```

fake で CI 検証済み。本番配布は未定。

## テスト

```bash
./gradlew :core:domain:jvmTest :core:data:jvmTest
./gradlew :androidApp:connectedFakeDebugAndroidTest
```

詳細は `docs/DEVELOPMENT.md` / `docs/VERIFY.md` を参照してください。
