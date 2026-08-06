---
layout: home
hero:
  name: DroidKaigi 2026 Quiz
  text: 会場向けクイズアプリ
  tagline: 参加者アプリと管理者アプリの使い方・仕様をまとめたドキュメントです。
  actions:
    - theme: brand
      text: 利用者向けを読む
      link: /user/overview
    - theme: alt
      text: 開発者向けを読む
      link: /developer/overview
features:
  - title: 参加者アプリ
    details: ニックネームを入力して単一選択・複数選択・並び替えのクイズに挑戦し、当日ランキングを確認します。
    link: /user/participant/flow
    linkText: 画面の流れ
  - title: 管理者アプリ
    details: Desktop の運営コンソールで問題セット・公開フォルダ・ランキングを管理します。
    link: /user/staff/overview
    linkText: 概要へ
  - title: 開発者向け
    details: モジュール構成、採点、fake/prod ランタイム、Firestore、ビルド手順をまとめています。
    link: /developer/overview
    linkText: 開発ドキュメント
---

## このサイトの読み方

| 読者 | 読む場所 |
|------|----------|
| 会場の参加者・スタッフ（使い方） | [利用者向け](/user/overview) |
| コントリビュータ・実装者 | [開発者向け](/developer/overview) |

開発向けの詳細（Gradle・モジュール境界・Firestore スキーマなど）は **開発者向け** セクションに分離しています。利用者向けページには紛れ込ませません。
