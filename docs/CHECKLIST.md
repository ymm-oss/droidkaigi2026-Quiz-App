# Implementation Checklist

> **進捗:** 34 / 34 完了  
> **最終更新:** 2026-07-09

## 使い方

1. 着手前に `[ ]` の未完了項目を 1 つ選ぶ（`docs/SPEC.md` の AC と照合）。
2. 実装・検証が終わったら、該当行の `[ ]` を `[x]` に変更する（Cursor / GitHub 上でクリック切り替え可）。
3. **確認** に書いたパス・コマンド・テストで完了を検証してからチェックする。
4. 新規項目は Phase 内で ID を連番（例: `P7-04`）。項目を削除せず `[x]` または `[ ]` を維持する。

## 進捗サマリー

| Phase | 内容 | 完了 |
|-------|------|------|
| 0 | 基盤 | 5 / 5 |
| 1 | Domain + Data | 5 / 5 |
| 2 | UI + Nav | 4 / 4 |
| 3 | Quiz UI | 3 / 3 |
| 4 | Ranking | 2 / 2 |
| 5 | Polish | 3 / 3 |
| 6 | Desktop / Web | 3 / 3 |
| 7 | UI リデザイン（Home 基準） | 6 / 6 |
| H | Harness iterate | 3 / 3 |
| 8 | Staff 認証 | 1 / 1 |

---

## Phase 0 — 基盤

- [x] **P0-01** Gradle KMP マルチモジュール — 確認: `./gradlew projects`
- [x] **P0-02** git / `.gitignore` — 確認: リポジトリルートに `.git` が存在
- [x] **P0-03** `docs/SPEC.md`, `docs/VERIFY.md` — 確認: 両ファイルが存在
- [x] **P0-04** `AGENTS.md`, `.cursor/rules`, `.cursor/skills` — 確認: 各ディレクトリにファイルあり
- [x] **P0-05** assembleDebug 成功 — 確認: `./gradlew :androidApp:assembleDebug`

## Phase 1 — Domain + Data

- [x] **P1-01** Question 3 種 + QuizSet — 確認: `core/domain/.../model/Question.kt`
- [x] **P1-02** QuizScorer + QuizEngine — 確認: `core/domain/.../scoring/`
- [x] **P1-03** JsonQuizRepository + quiz_set.json — 確認: `core/data/.../quiz_set.json`
- [x] **P1-04** FakeRankingRepository + SubmitScoreUseCase — 確認: `core/data/`, `core/domain/.../usecase/`
- [x] **P1-05** commonTest（採点・ランキング） — 確認: `./gradlew :core:domain:cleanTest :core:data:cleanTest`

## Phase 2 — UI + Nav

- [x] **P2-01** QuizTheme / QuizTokens — 確認: `core/ui/.../theme/QuizTheme.kt`
- [x] **P2-02** Nav3 + adaptive shell — 確認: `composeApp/.../QuizNavHost.kt`
- [x] **P2-03** Home smoke androidTest — 確認: `androidApp/.../HomeScreenTest.kt`
- [x] **P2-04** 回答完了後は Quiz に戻れない — 確認: `QuizNavHost.navigateToResult` が Quiz を replace、回答中の Quiz からは `onBack` で Home へ戻れる

## Phase 3 — Quiz UI

- [x] **P3-01** Single / Multiple / Reorder UI — 確認: `feature/quiz/.../QuizScreen.kt`
- [x] **P3-02** Quiz → Result フロー — 確認: Nav 上でクイズ完了後に Result へ遷移
- [x] **P3-03** QuizFlowAndroidTest — 確認: `androidApp/.../QuizFlowAndroidTest.kt`

## Phase 4 — Ranking

- [x] **P4-01** Ranking 画面 + ハイライト — 確認: `feature/ranking/.../RankingScreen.kt`
- [x] **P4-02** RankingAndroidTest — 確認: `androidApp/.../RankingAndroidTest.kt`

## Phase 5 — Polish

- [x] **P5-01** アニメーション（選択・スコア・ランキング） — 確認: `QuizMotion.kt`, `quiz-animation.mdc`
- [x] **P5-02** README — 確認: リポジトリルートの `README.md`
- [x] **P5-03** VERIFY 手順 — 確認: `docs/VERIFY.md`

## Phase 6 — Desktop

- [x] **P6-01** KMP `jvm()` ターゲット（composeApp / core / feature） — 確認: 各 `build.gradle.kts` に `jvm()`
- [x] **P6-02** `:desktopApp` エントリ — 確認: `desktopApp/src/main/kotlin/.../Main.kt`
- [x] **P6-03** `:wasmApp` + 各モジュール `wasmJs()` — 確認: `./gradlew :wasmApp:compileKotlinWasmJs`、`:wasmApp:wasmJsBrowserDevelopmentRun`

## Phase 7 — UI リデザイン（Home 基準）

- [x] **P7-01** QuizTokens / QuizTypography 拡張 — 確認: `QuizTokens.kt`, `QuizTypography.kt`
- [x] **P7-02** 共通コンポーネント（Background, Card, Button, TextField, Hero） — 確認: `core/ui/.../components/Quiz*.kt`（5 ファイル）
- [x] **P7-03** Home 画面リデザイン — 確認: `HomeScreen.kt` が `QuizScreenBackground` 等を使用
- [x] **P7-04** Quiz 画面へデザイン適用 — 確認: `QuizScreen.kt` が `QuizScreenBackground` + 共通コンポーネントを使用
- [x] **P7-05** Result 画面へデザイン適用 — 確認: `ResultScreen.kt` が `QuizScreenBackground` + 共通コンポーネントを使用
- [x] **P7-06** Ranking 画面へデザイン適用 — 確認: `RankingScreen.kt` が `QuizScreenBackground` + 共通コンポーネントを使用

## Phase 8 — Staff 認証

- [x] **P8-01** スタッフ認証画面（スタッフアプリのみ） — 確認: `./gradlew :staffDesktopApp:run` でログイン画面 → `staff@droidkaigi.local` / `staff2026` → コンソール、`feature/staff/.../auth/StaffAuthScreen.kt`

## Harness iterate（各 Phase 後）

- [x] **H-01** quiz-domain-data.mdc 追記 — 確認: `.cursor/rules/quiz-domain-data.mdc`
- [x] **H-02** quiz-feature.mdc / quiz-animation ルール — 確認: `.cursor/rules/quiz-feature.mdc`, `quiz-animation.mdc`
- [x] **H-03** droidkaigi-quiz-verify スキル — 確認: `.cursor/skills/droidkaigi-quiz-verify/SKILL.md`
