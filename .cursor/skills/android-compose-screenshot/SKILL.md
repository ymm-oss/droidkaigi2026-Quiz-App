---
name: android-compose-screenshot
description: Capture deterministic PNG screenshots of Android Compose UI from instrumented tests. Use when taking Android app screenshots for review, adding an androidTest screenshot harness, or replacing adb/`android` CLI manual capture.
---

# Android Compose Screenshot

Capture Android screenshots with an **instrumented Compose UI test that renders a stateless
`XxxContent` composable directly**. Never drive the live app with taps to reach a screen.

Live-driving (`android` CLI / `adb shell input tap`) is unreliable and has repeatedly failed here:
Play Protect and Google sign-in dialogs steal focus, taps near the bottom hit the gesture-nav area
and background the app, `keyevent 111` behaves as BACK, coordinates drift between densities, and
`monkey -p com.droidkaigi.quiz` launches nothing because the flavor adds `.fake` to the
applicationId. The test below avoids all of that.

## 1. Wire up dependencies

Screenshot tests live in `:androidApp`'s `androidTest` and render feature composables directly:

```kotlin
// androidApp/build.gradle.kts
androidTestImplementation(libs.compose.ui.test.junit4)
androidTestImplementation(libs.androidx.testExt.junit)
androidTestImplementation(projects.core.domain)
androidTestImplementation(projects.core.ui)
androidTestImplementation(projects.feature.quiz)
```

## 2. Declare the host activity (required, easy to get wrong)

`createComposeRule()` launches a bare `ComponentActivity`. It must be declared in the **app's debug
manifest**, not in `src/androidTest/AndroidManifest.xml`:

```xml
<!-- androidApp/src/debug/AndroidManifest.xml -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application>
        <activity
            android:name="androidx.activity.ComponentActivity"
            android:exported="false"
            android:theme="@android:style/Theme.Material.Light.NoActionBar" />
    </application>
</manifest>
```

- Missing entirely → `RuntimeException: Unable to resolve activity for: ... ComponentActivity`
- Declared in `src/androidTest/` → `Intent in process com.droidkaigi.quiz.fake resolved to
  different process com.droidkaigi.quiz.fake.test`
- Do **not** use `createAndroidComposeRule<MainActivity>()` + `setContent`; `MainActivity` already
  calls `setContent` and the rule throws.

## 3. Write output to `additionalTestOutputDir`

AGP passes `additionalTestOutputDir` to the runner and pulls its contents to the host after the run.
Do **not** write to `getExternalFilesDir()` directly: on API 30+ the adb shell user cannot read
`/sdcard/Android/data/<pkg>/`, so `adb pull` fails with `No such file or directory`, and `run-as`
is unavailable on Google Play emulator images.

```kotlin
private fun outputDir(): File {
    val fromAgp = InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")
    val dir = fromAgp?.let(::File)
        ?: InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null)!!
    dir.mkdirs()
    return dir
}

private fun capture(fileName: String) {
    val bitmap = composeRule.onRoot().captureToImage().asAndroidBitmap()
    FileOutputStream(File(outputDir(), fileName)).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
}
```

## 4. Test pattern

Fixed state, no-op callbacks, explicit locale. One `@Test` per captured state.

```kotlin
@RunWith(AndroidJUnit4::class)
class QuizFeedbackScreenshotAndroidTest {
    @get:Rule val composeRule = createComposeRule()

    private fun render(state: QuizUiState) {
        composeRule.setContent {
            CompositionLocalProvider(LocalAppLocale provides "ja") {
                key("ja") { QuizTheme { QuizContent(state = state, /* no-op callbacks */) } }
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun captureCorrectFeedbackOverlay() {
        render(state(correct = true))
        capture("android-01-feedback-correct.png")
    }
}
```

Reference implementation:
`androidApp/src/androidTest/kotlin/com/droidkaigi/quiz/QuizFeedbackScreenshotAndroidTest.kt`.

## 5. Run and collect

```bash
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
export PATH="$ANDROID_HOME/platform-tools:$PATH"
export ANDROID_SERIAL=emulator-5554   # required when more than one device is attached

adb -s "$ANDROID_SERIAL" shell settings put global window_animation_scale 0
adb -s "$ANDROID_SERIAL" shell settings put global transition_animation_scale 0
adb -s "$ANDROID_SERIAL" shell settings put global animator_duration_scale 0

./gradlew :androidApp:connectedFakeDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.droidkaigi.quiz.QuizFeedbackScreenshotAndroidTest

cp "androidApp/build/outputs/connected_android_test_additional_output/fakeDebugAndroidTest/connected/"*/*.png \
  docs/screenshots/android/
```

Notes:
- `ANDROID_HOME` must be exported or Gradle fails with `SDK location not found`.
- Always filter with `-Pandroid.testInstrumentationRunnerArguments.class=...`; the repo's other
  instrumented tests are flaky and slow.
- The device directory name contains the AVD name and spaces — quote the glob.
- adb/Gradle need to run outside the agent sandbox (`required_permissions: ["all"]`).

## 6. Verify

1. `BUILD SUCCESSFUL` and `Finished N tests`.
2. Read every PNG and check clipping, empty surfaces, wrong locale, and the intended state.
3. Commit PNGs under `docs/screenshots/android/` when they are review artifacts.

## Common failures

| Symptom | Cause / fix |
| --- | --- |
| `Unable to resolve activity ... ComponentActivity` | Host activity not declared → add `androidApp/src/debug/AndroidManifest.xml` |
| `resolved to different process ....test` | Host activity declared in `src/androidTest/` → move it to `src/debug/` |
| `adb pull` → `No such file or directory` | Wrote to `getExternalFilesDir()` → use `additionalTestOutputDir` |
| `run-as: unknown package` | Play emulator image; don't rely on `run-as` |
| `SDK location not found` | Export `ANDROID_HOME` before Gradle |
| Test runs twice / wrong device | Multiple devices attached → set `ANDROID_SERIAL` |
| App backgrounds itself, wrong screen captured | You are live-driving with taps → switch to this test-based capture |
