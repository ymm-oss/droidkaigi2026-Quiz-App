# Runtime (fake / prod)

Exactly one data-layer source set is compiled per build (`fakeMain` or `prodMain`).

| Variant | `quiz.runtime` | Questions | Ranking | Use |
|---------|----------------|-----------|---------|-----|
| **fake** (default) | `fake` | Bundled `quiz_set.json` | In-memory | Offline UI/scoring |
| **prod** | `prod` | Firestore | Firestore | Venue / integration |

UI modules are shared; only Repository bindings switch.

## Switching

| Platform | How |
|----------|-----|
| Android | Build Variant (`fakeDebug` / `prodDebug`) |
| Desktop / staff | `gradle.properties` or `-Pquiz.runtime=prod` |

Always **rebuild** after switching.

## Caveats

- fake is a **dev harness**, not a production substitute
- No silent fallback to fake on network failure
- Wasm prod is unsupported (startup error)
- Do not assemble both Android flavors in one Gradle invocation (KMP falls back to fake)

See `docs/DEVELOPMENT.md` for full steps.
