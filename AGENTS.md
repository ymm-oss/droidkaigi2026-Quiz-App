# AGENTS — DroidKaigi Quiz

Package: `com.droidkaigi.quiz`

## Modules

| Module | Role |
|--------|------|
| `:composeApp` | Nav3, adaptive shell, Android/Desktop entry |
| `:core:domain` | Models, scoring, use cases |
| `:core:data` | Repositories, JSON, `AppDependencies.shared` |
| `:core:ui` | `QuizTheme`, tokens, shared components |
| `:feature:quiz` | Home, Quiz, Result |
| `:feature:ranking` | Ranking list |

Dependency direction: `feature → core:ui, domain` · `data → domain` · `composeApp → feature` (no reverse).

## Docs & harness

- Spec: [docs/SPEC.md](docs/SPEC.md)
- Checklist: [docs/CHECKLIST.md](docs/CHECKLIST.md)
- Verify: [docs/VERIFY.md](docs/VERIFY.md)
- Rules: [.cursor/rules/](.cursor/rules/) (`quiz-app-core`, `quiz-feature`, `quiz-domain-data`, `quiz-animation`)
- Skills: [.cursor/skills/](.cursor/skills/)

## External skills

- `~/.claude/skills/navigation-3`
- `~/.claude/skills/adaptive`
- `~/.claude/skills/android-cli`
- `~/.claude/skills/testing-setup`
