# Architecture

## Modules

| Module | Role |
|--------|------|
| `:androidApp` | Android entry (`MainActivity`) |
| `:desktopApp` | Desktop entry (`main`) |
| `:wasmApp` | Web (Wasm) entry |
| `:composeApp` | Shared UI + Nav3 (routes live here) |
| `:staffComposeApp` / `:staffDesktopApp` | Staff console (JVM) |
| `:core:domain` | Models, scoring, use cases |
| `:core:data` | Repositories, Metro, `fakeMain`/`prodMain` |
| `:core:ui` | `QuizTheme`, tokens, shared components |
| `:feature:quiz` | Home / Quiz / Result |
| `:feature:ranking` | Ranking |
| `:feature:staff` | Staff UI (JVM only) |

## Dependency direction

```
feature → core:ui, core:domain
data → domain
composeApp → feature
staffComposeApp → feature:staff
```

No reverse dependencies. Nav routes only in `composeApp`. Do not add `main()` / `MainActivity` to `composeApp`.

## MVI per screen

- `XxxUiState` / `XxxIntent` / `XxxEvent` / `XxxViewModel`
- Business logic in domain use cases

## Shared graph

Use only `AppDependencies.shared`, initialized once via `initQuizAppGraph()` (Metro).

## Theming

Wrap with `QuizTheme { }`. Colors from `QuizTokens` / `QuizColors` — no raw `Color(0x…)` in features.
