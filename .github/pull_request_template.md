<!-- この PR で何を達成するか、1〜3 文で要約してください。タイトルと重複しても構いません。 -->

## 実装概要



<!-- なぜこの変更が必要か。Issue 番号、Slack での相談、会場運営上の理由などを書いてください。 -->

## 背景



<!-- 変更の中身を箇条書きで。モジュール名・画面名・主要クラスなど、レビュアーが diff の当たりをつけやすい粒度で。 -->

## 実装内容

-

<!-- スクリーンショット、動画、ログ、Before/After があればここか下に貼ってください。 -->

### 具体的なもの

-

<!-- マージ前の確認項目。[docs/CONTRIBUTING.md](../docs/CONTRIBUTING.md) の PR チェックリストに準拠。該当しない行は削除して OK。 -->

## PR チェックリスト

- [ ] [docs/SPEC.md](../docs/SPEC.md) の該当 AC を満たしている
- [ ] 機能追加・仕様変更時は [docs/CHECKLIST.md](../docs/CHECKLIST.md) の該当 ID を更新（該当する場合）
- [ ] ユニットテスト: `./gradlew :core:domain:jvmTest :core:data:jvmTest`
- [ ] UI 変更時は `./gradlew :androidApp:connectedDebugAndroidTest`（エミュレータ要）または [docs/VERIFY.md](../docs/VERIFY.md) で手動確認
- [ ] `quiz.runtime` や Firestore 周りを触った場合は prod ビルド・結合確認の手順を PR 説明に記載

<!-- 設計判断・影響範囲・不安な点など、レビュアーに重点的に見てほしい箇所。なければセクションごと削除して OK。 -->

## 特にみてほしいもの

-
