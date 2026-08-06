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

- Spec site (VitePress / GitHub Pages): [docs/README.md](docs/README.md) · local `cd docs && npm ci && npm run docs:dev`
- Contributing: [docs/CONTRIBUTING.md](docs/CONTRIBUTING.md)
- Development (build, fake/prod): [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md)
- Spec: [docs/SPEC.md](docs/SPEC.md)
- Firestore (prod DB): [docs/FIRESTORE.md](docs/FIRESTORE.md) · CLI: [firebase.json](firebase.json)
- Verify: [docs/VERIFY.md](docs/VERIFY.md)
- Rules: [.cursor/rules/](.cursor/rules/) (`quiz-app-core`, `quiz-feature`, `quiz-domain-data`, `quiz-animation`, `quiz-android-pr-screenshots`, `quiz-staff-pr-screenshots`, `quiz-stitch-source`)

## Skills

[.cursor/skills/](.cursor/skills/) — タスクに応じて読み込む。

| Skill | 用途 |
|-------|------|
| [`droidkaigi-quiz`](.cursor/skills/droidkaigi-quiz/SKILL.md) | 実装マスターワークフロー。新画面・機能・修正の起点（domain → data → UI → feature → nav → test → verify → review） |
| [`droidkaigi-quiz-test`](.cursor/skills/droidkaigi-quiz-test/SKILL.md) | テストの追加・実行（`commonTest` / `androidInstrumentedTest`） |
| [`droidkaigi-quiz-verify`](.cursor/skills/droidkaigi-quiz-verify/SKILL.md) | Android CLI + スクショで手順どおりに動作確認する |
| [`droidkaigi-quiz-device-review`](.cursor/skills/droidkaigi-quiz-device-review/SKILL.md) | 参加者 Android を実機で通しレビューし、UI/UX・画面サイズ・アクセシビリティ・保守性の指摘を Issue 化する |
| [`droidkaigi-quiz-review`](.cursor/skills/droidkaigi-quiz-review/SKILL.md) | PR / ブランチ変更のコードレビュー（CI の Cursor Code Review と同じ観点） |
| [`android-pr-screenshots`](.cursor/skills/android-pr-screenshots/SKILL.md) | 参加者 Android の UI 変更 PR にスクショを比較表で埋め込む（必須） |
| [`android-compose-screenshot`](.cursor/skills/android-compose-screenshot/SKILL.md) | instrumented テストから Android Compose UI の決定的 PNG を撮る撮影ハーネス |
| [`staff-pr-screenshots`](.cursor/skills/staff-pr-screenshots/SKILL.md) | スタッフ（管理者）UI 変更 PR にスクショを比較表で埋め込む（必須） |
| [`jvm-compose-screenshot`](.cursor/skills/jvm-compose-screenshot/SKILL.md) | JVM/Desktop Compose UI テストから決定的 PNG を撮る撮影ハーネス |
| [`docs-bilingual-sync`](.cursor/skills/docs-bilingual-sync/SKILL.md) | VitePress 仕様書の JA / EN を常に両立させる（ページ追加・更新時） |

## External skills

`~/.claude/skills/` — 汎用の外部スキル。

| Skill | 用途 |
|-------|------|
| [`navigation-3`](~/.claude/skills/navigation-3) | Jetpack Navigation 3 の導入・移行・パターン実装 |
| [`adaptive`](~/.claude/skills/adaptive) | 端末サイズ（スマホ / タブレット / Foldable など）に応じた適応 UI |
| [`android-cli`](~/.claude/skills/android-cli) | `android` CLI でのビルド・実行・端末操作・スクショ・レイアウト調査 |
| [`testing-setup`](~/.claude/skills/testing-setup) | Android のテスト基盤・ハーネス整備 |
