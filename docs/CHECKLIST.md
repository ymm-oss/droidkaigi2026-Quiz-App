# Implementation Checklist

## Phase 0 — 基盤
- [x] Gradle KMP マルチモジュール
- [x] git / .gitignore
- [x] docs/SPEC.md, VERIFY.md
- [x] AGENTS.md, .cursor/rules, .cursor/skills
- [x] assembleDebug 成功

## Phase 1 — Domain + Data
- [x] Question 3 種 + QuizSet
- [x] QuizScorer + QuizEngine
- [x] JsonQuizRepository + quiz_set.json
- [x] FakeRankingRepository + SubmitScoreUseCase
- [x] commonTest（採点・ランキング）

## Phase 2 — UI + Nav
- [x] QuizTheme / QuizTokens
- [x] Nav3 + adaptive shell
- [x] Home smoke androidTest

## Phase 3 — Quiz UI
- [x] Single / Multiple / Reorder UI
- [x] Quiz → Result フロー
- [x] QuizFlowAndroidTest

## Phase 4 — Ranking
- [x] Ranking 画面 + ハイライト
- [x] RankingAndroidTest

## Phase 5 — Polish
- [x] アニメーション（選択・スコア・ランキング）
- [x] README
- [x] VERIFY 手順

## Phase 6 — Desktop
- [x] jvm() + Main.kt

## Phase 7 — UI リデザイン（Home 基準）
- [x] QuizTokens / QuizTypography 拡張
- [x] 共通コンポーネント（Background, Card, Button, TextField, Hero）
- [x] Home 画面リデザイン
- [ ] Quiz 画面へデザイン適用
- [ ] Result 画面へデザイン適用
- [ ] Ranking 画面へデザイン適用

## Harness iterate（各 Phase 後）
- [x] quiz-domain-data.mdc 追記（JSON パス、採点 domain のみ）
- [x] quiz-feature.mdc / quiz-animation 相当の core ルール
- [x] droidkaigi-quiz-verify スキル
