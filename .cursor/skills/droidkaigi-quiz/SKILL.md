---
name: droidkaigi-quiz
description: Master workflow for DroidKaigi 2026 Quiz — new screens, features, and fixes. Use when implementing quiz, ranking, or navigation in this repo.
---

# DroidKaigi Quiz — implementation workflow

## Before coding

1. Read AC in `docs/SPEC.md`
2. Pick one open item in `docs/CHECKLIST.md`
3. Confirm module from `AGENTS.md`

## Order

1. **Domain** — models, `QuizScorer` / use cases, `commonTest`
2. **Data** — repository if needed, wire `AppDependencies.shared`
3. **UI** — tokens/components in `core:ui` if reusable
4. **Feature** — Screen + ViewModel (MVI 4 files)
5. **Nav** — register route in `composeApp/.../QuizNavHost.kt`
6. **Test** — see `.cursor/skills/droidkaigi-quiz-test/SKILL.md`
7. **Verify** — see `.cursor/skills/droidkaigi-quiz-verify/SKILL.md`

## External skills

- Nav3: `~/.claude/skills/navigation-3`
- Adaptive: `~/.claude/skills/adaptive`
- Android CLI: `~/.claude/skills/android-cli`
- Testing: `~/.claude/skills/testing-setup`

## After phase

- Mark checklist `[x]`
- Extract repeated rules into `.cursor/rules/*.mdc` (one line each)
- Update `AGENTS.md` links only if new skill/rule file added
