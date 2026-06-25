# AGENTS — DroidKaigi Quiz

Package: `com.droidkaigi.quiz`

## Modules

| Module | Role |
|--------|------|
| `:androidApp` | Android entry (`MainActivity`) — `com.android.application` |
| `:desktopApp` | Desktop entry (`main`) — `kotlin.jvm` + Compose Desktop |
| `:staffDesktopApp` | Staff desktop entry — `kotlin.jvm` + Compose Desktop; `quiz.runtime=fake` (dev) or `prod` (venue Firestore) |
| `:staffComposeApp` | Staff shared UI (`StaffApp`) — JVM only; Metro graph follows `quiz.runtime` (`FakeStaffQuizAppGraph` / `ProdStaffQuizAppGraph`) |
| `:wasmApp` | Web entry (`main`) — `wasmJs` + `ComposeViewport` |
| `:composeApp` | Shared UI + Nav3 (`App`, routes) — `androidKmpLibrary` + `jvm` + `wasmJs` |
| `:core:domain` | Models, scoring, use cases |
| `:core:data` | Repositories, Metro bindings, `fakeMain`/`prodMain`, `AppDependencies.shared` |
| `:core:ui` | `QuizTheme`, tokens, shared components |
| `:feature:quiz` | Home, Quiz, Result |
| `:feature:ranking` | Ranking list |
| `:feature:staff` | Staff console (quiz preview + rankings) — JVM only |

Dependency direction: `feature → core:ui, domain` · `data → domain` · `composeApp → feature` · `staffComposeApp → feature:staff` (no reverse).

## Docs & harness

- Contributing: [docs/CONTRIBUTING.md](docs/CONTRIBUTING.md)
- Development (build, fake/prod): [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md)
- Spec: [docs/SPEC.md](docs/SPEC.md)
- Firestore (prod DB): [docs/FIRESTORE.md](docs/FIRESTORE.md) · CLI: [firebase.json](firebase.json)
- Checklist: [docs/CHECKLIST.md](docs/CHECKLIST.md)
- Verify: [docs/VERIFY.md](docs/VERIFY.md)
- Rules: [.cursor/rules/](.cursor/rules/) (`quiz-app-core`, `quiz-feature`, `quiz-domain-data`, `quiz-animation`)
- Skills: [.cursor/skills/](.cursor/skills/) (`droidkaigi-quiz`, `droidkaigi-quiz-test`, `droidkaigi-quiz-verify`, `droidkaigi-quiz-review`)

## External skills

- `~/.claude/skills/navigation-3`
- `~/.claude/skills/adaptive`
- `~/.claude/skills/android-cli`
- `~/.claude/skills/testing-setup`
