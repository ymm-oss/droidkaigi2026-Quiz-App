---
name: jvm-compose-screenshot
description: Capture deterministic PNG screenshots from Compose Multiplatform JVM/Desktop UI tests. Use when taking desktop app screenshots, adding screenshot harnesses, or replacing manual JVM window capture.
---

# JVM Compose Screenshot

Use an off-screen Compose UI test instead of launching the Desktop window and using OS capture.

## Dependencies

Add these to the target KMP module's `jvmTest` source set:

```kotlin
jvmTest.dependencies {
    implementation(libs.kotlin.test)
    implementation(libs.compose.ui.test.junit4)
    implementation(compose.desktop.currentOs)
}
```

Add other runtime dependencies used by rendered content explicitly when they are not exported transitively.

## Capture helper

Use `runDesktopComposeUiTest` for a fixed viewport and capture the complete Skiko surface. Surface capture includes dialogs and avoids `onRoot()` failures when a dialog creates multiple semantics roots.

```kotlin
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import java.io.File

@OptIn(ExperimentalTestApi::class)
fun DesktopComposeUiTest.capturePng(file: File) {
    val bitmap = captureToImage()
    val data = Image.makeFromBitmap(bitmap.asSkiaBitmap())
        .encodeToData(EncodedImageFormat.PNG)
        ?: error("Failed to encode ${file.name}")
    file.parentFile?.mkdirs()
    file.writeBytes(data.bytes)
}
```

## Test pattern

Render stateless content with fixed sample data. Do not initialize production repositories or depend on wall-clock/network state.

```kotlin
@OptIn(ExperimentalTestApi::class)
@Test
fun captureScreen() = runDesktopComposeUiTest(width = 1440, height = 900) {
    setContent {
        QuizTheme {
            ScreenContent(/* deterministic state and no-op callbacks */)
        }
    }

    onNodeWithText("Expected title").assertIsDisplayed()
    capturePng(outputFile("screen.png"))
}
```

## Output

- Resolve the output directory from a Gradle system property so the test is independent of `user.dir`.
- Use stable numbered names when screenshots describe a flow.
- Keep generated PNGs in `docs/screenshots/<feature>/` only when they are review artifacts; otherwise write under `build/screenshots/`.

```kotlin
tasks.withType<Test>().configureEach {
    systemProperty(
        "screenshot.dir",
        rootProject.layout.projectDirectory.dir("docs/screenshots/<feature>").asFile.absolutePath,
    )
}
```

## Verification

1. Run only the screenshot class first:

   ```bash
   ./gradlew :<module>:jvmTest --tests '<package>.ScreenshotJvmUiTest'
   ```

2. Confirm `BUILD SUCCESSFUL`.
3. Read every generated PNG and check clipping, empty surfaces, dialogs, and expected state.
4. Run the module's full `jvmTest` before handoff.

## Common failures

- **Multiple roots from dialogs**: call `DesktopComposeUiTest.captureToImage()`, not `onRoot().captureToImage()`.
- **`NoClassDefFoundError` while rendering**: add the missing runtime library to `jvmTest.dependencies`.
- **Wrong output path**: pass an absolute project-root path through a test system property.
- **Animations or loading states**: use fixed stateless content, then assert a stable node before capture.
- **Manual screenshots differ by machine**: keep viewport, data, locale, and theme explicit in the test.
