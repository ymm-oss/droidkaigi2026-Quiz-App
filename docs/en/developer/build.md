# Build & run

Requires AGP 9.x + Gradle 9.4, JDK 17+ (Desktop).

## Participant — Android

```bash
./gradlew :androidApp:assembleFakeDebug
./gradlew :androidApp:assembleProdDebug
```

Switch **Build Variants** in Android Studio, then Rebuild.

## Participant — Desktop

```bash
./gradlew :desktopApp:run
./gradlew :desktopApp:run -Pquiz.runtime=prod
```

## Staff — Desktop

```bash
./gradlew :staffDesktopApp:run
./gradlew :staffDesktopApp:run -Pquiz.runtime=prod
```

Run configs: `staffDesktop[Fake]` / `staffDesktop[Prod]`.

| Runtime | Auth |
|---------|------|
| fake | Local fixed account (+ demo one-click) |
| prod | Firebase Auth (organizer credentials) |

## Web (Wasm)

```bash
./gradlew :wasmApp:wasmJsBrowserDevelopmentRun
```

fake verified in CI. Production distribution TBD.

## Tests

```bash
./gradlew :core:domain:jvmTest :core:data:jvmTest
./gradlew :androidApp:connectedFakeDebugAndroidTest
```

See `docs/DEVELOPMENT.md` / `docs/VERIFY.md`.
