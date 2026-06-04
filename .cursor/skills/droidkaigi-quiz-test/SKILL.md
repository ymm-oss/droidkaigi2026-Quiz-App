---
name: droidkaigi-quiz-test
description: Add or run tests for DroidKaigi Quiz — commonTest and androidInstrumentedTest.
---

# Test DroidKaigi Quiz

## Domain / data (JVM)

```bash
./gradlew :core:domain:jvmTest :core:data:jvmTest
```

Add tests under `src/commonTest/kotlin/`.

## Android UI

```bash
./gradlew :composeApp:connectedDebugAndroidTest
```

Use `createAndroidComposeRule<MainActivity>()` and `onNodeWithText`.

## Gates

- Scoring changes → `QuizScorerTest` must pass
- New screen → at least one UI test or VERIFY screenshot noted in PR
