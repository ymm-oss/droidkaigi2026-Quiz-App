# Manual verification (Android CLI)

## Runtime (`fake` / `prod`)

Set in `gradle.properties` (or override per build):

```properties
quiz.runtime=fake   # bundled JSON + local ranking (default)
# quiz.runtime=prod # RemoteQuizRepository / RemoteRankingRepository (Firebase etc.)
```

```bash
./gradlew :androidApp:assembleDebug -Pquiz.runtime=prod
```

Rebuild after changing runtime (inactive `fakeMain` / `prodMain` is not compiled).

## Staff desktop (`fake` in-memory)

```bash
./gradlew :staffDesktopApp:run
```

- **クイズ**: bundled JSON の問題一覧と正解（スタッフ向け表示）
- **ランキング**: `FakeRankingRepository` の当日スコア（デモ3件 + 参加者アプリからの submit は別プロセスのため反映されない）

## Prerequisites

- Android SDK and emulator or device
- `android` CLI on PATH

## Steps

1. Build: `./gradlew :androidApp:assembleDebug`
2. Install & run: `android run --path androidApp/build/outputs/apk/debug/androidApp-debug.apk` (or run **androidApp** from Android Studio)
3. Flow: Home → enter nickname → Quiz (3 questions) → Result → Ranking
4. Screenshot: `android screenshot --output docs/screenshots/flow-$(date +%Y%m%d).png`
5. On UI issues: `android layout` for hierarchy JSON

## Required screenshots (release gate)

- `docs/screenshots/home.png`
- `docs/screenshots/quiz.png`
- `docs/screenshots/result.png`
- `docs/screenshots/ranking.png`
