---
name: droidkaigi-quiz
description: Master workflow for DroidKaigi 2026 Quiz — new screens, features, and fixes. Use when implementing quiz, ranking, or navigation in this repo.
---

# DroidKaigi Quiz — implementation workflow

## Before coding

1. Read AC in `docs/SPEC.md`
2. Confirm module from `AGENTS.md`
3. Visual UI changes: follow `.cursor/rules/quiz-stitch-source.mdc` — **feedback → Stitch → implement from Stitch** (do not finalize layout in code first)

## Order

1. **Domain** — models, `QuizScorer` / use cases, `commonTest`
2. **Data** — repository if needed, wire `AppDependencies.shared`
3. **Stitch** (visual UI) — update design from feedback; treat Stitch proposal as primary
4. **UI** — tokens/components in `core:ui` if reusable
5. **Feature** — Screen + ViewModel (MVI 4 files)
6. **Nav** — register route in `composeApp/.../QuizNavHost.kt`
7. **Test** — see `.cursor/skills/droidkaigi-quiz-test/SKILL.md`
8. **Verify** — see `.cursor/skills/droidkaigi-quiz-verify/SKILL.md`
9. **Staff UI PR screenshots** — 管理者アプリ UI 変更時は `.cursor/skills/staff-pr-screenshots/SKILL.md`（必須・比較表で埋め込み）
10. **Review** — see `.cursor/skills/droidkaigi-quiz-review/SKILL.md`（PR 前）

参加者アプリを実機で通しレビューして指摘を Issue 化するときは `.cursor/skills/droidkaigi-quiz-device-review/SKILL.md`

## External skills

- Nav3: `~/.claude/skills/navigation-3`
- Adaptive: `~/.claude/skills/adaptive`
- Android CLI: `~/.claude/skills/android-cli`
- Testing: `~/.claude/skills/testing-setup`

## After phase

- Update progress summary counts and **最終更新** date in the checklist header
- Extract repeated rules into `.cursor/rules/*.mdc` (one line each)
- Update `AGENTS.md` links only if new skill/rule file added
