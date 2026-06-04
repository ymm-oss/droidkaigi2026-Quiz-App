---
name: droidkaigi-quiz-verify
description: Manual verification for DroidKaigi Quiz using Android CLI and screenshots.
---

# Verify DroidKaigi Quiz

## Build

```bash
./gradlew :composeApp:assembleDebug
```

## Run

Use Android Studio Run, or Android CLI per `~/.claude/skills/android-cli`.

## Screenshot

```bash
mkdir -p docs/screenshots
android screenshot --output docs/screenshots/home.png
```

Capture: Home, Quiz (each type), Result, Ranking.

## Debug layout

```bash
android layout
```

## Full flow

Home → nickname → start → answer 3 questions → Result → Ranking → Home

See `docs/VERIFY.md`.
