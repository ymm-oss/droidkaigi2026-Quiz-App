# Contributing

PRs are welcome. Details: `docs/CONTRIBUTING.md`.

## Where to ask

| Channel | Best for |
|---------|----------|
| Slack `#191_eve_droidkaigi` | Quick questions, venue ops |
| [GitHub Issues](https://github.com/ymm-oss/droidkaigi2026-Quiz-App/issues) | Design, bugs, feature tracking |

## Workflow

1. Discuss on Issue/Slack if needed
2. Branch from `master`
3. Implement with fake runtime
4. Test and verify
5. Open PR (1 approve to merge)

## PR checklist (highlights)

- [ ] Meets relevant AC in `docs/SPEC.md`
- [ ] `jvmTest` passes
- [ ] Embed screenshots for UI PRs (Android participant / staff Desktop skills)
- [ ] Document prod verification if touching `quiz.runtime` / Firestore

## AI-assisted development

Cursor-oriented assets:

- `AGENTS.md`
- `.cursor/rules/`
- `.cursor/skills/` (implement, test, verify, review, screenshots)
