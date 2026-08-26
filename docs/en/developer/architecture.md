# Architecture

## Modules

| Module | Role |
|--------|------|
| `:androidApp` | Android entry (`MainActivity`) |
| `:desktopApp` | Desktop entry (`main`) |
| `:wasmApp` | Web (Wasm) entry. `/` is the participant app, `/staff` is the staff console |
| `:composeApp` | Shared UI + Nav3 (participant routes live here) |
| `:staffComposeApp` / `:staffDesktopApp` | Staff console (JVM Desktop; Web hosts `staffComposeApp` from wasm) |
| `:core:domain` | Models, scoring, use cases |
| `:core:data` | Repositories, Metro, `fakeMain`/`prodMain` |
| `:core:ui` | `QuizTheme`, tokens, shared components |
| `:feature:quiz` | Home / Quiz / Result |
| `:feature:ranking` | Ranking |
| `:feature:staff` | Staff UI (JVM + wasmJs) |

## Dependency direction

```
feature → core:ui, core:domain
data → domain
composeApp → feature:quiz, feature:ranking
staffComposeApp → feature:staff, feature:quiz
wasmApp → composeApp, staffComposeApp
```

No reverse dependencies. Participant Nav routes only in `composeApp`. Staff Web starts `StaffApp` at `/staff` and is not mixed into Nav3. Do not add `main()` / `MainActivity` to `composeApp`.

## MVI per screen

- `XxxUiState` / `XxxIntent` / `XxxEvent` / `XxxViewModel`
- Business logic in domain use cases

## Shared graph

Use only `AppDependencies.shared`, initialized once via Metro. Participant uses `initQuizAppGraph()`; staff (Desktop / Wasm `/staff`) uses `initStaffQuizAppGraph()`.

## Theming

Wrap with `QuizTheme { }`. Colors from `QuizTokens` / `QuizColors` — no raw `Color(0x…)` in features.
